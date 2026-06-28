package com.example.iis.repository;

import com.example.iis.model.Process;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProcessRepository extends JpaRepository<Process, Long> {
    interface PhaseCountView {
        String getPhaseName();

        Long getOrderCount();
    }

    interface TransitionRateView {
        String getFromPhase();

        String getToPhase();

        Long getReachedCount();

        Long getTransitionedCount();

        Double getRatePercent();
    }

    interface AverageDurationView {
        String getPhaseName();

        Long getAverageDurationSeconds();

        Long getSampleCount();
    }

    interface LongestActiveOrderView {
        String getPhaseName();

        Long getPhaseRank();

        Long getProcessId();

        Long getOfferId();

        Long getCustomerId();

        String getCustomerName();

        String getCustomerEmail();

        java.time.LocalDateTime getPhaseStartTime();

        Long getDurationSeconds();
    }

    List<Process> findAllByOrderByStartTimeDesc();

    @Query("""
            select distinct proc from Process proc
            join proc.phases phase
            where phase.offer.id = :offerId and proc.customer.id = :customerId
            """)
    Optional<Process> findByOfferIdAndCustomerId(
            @Param("offerId") Long offerId,
            @Param("customerId") Long customerId
    );

    @Query(value = """
            with ordered_phases(phase_name, sort_order) as (
                values
                    ('Ponuda', 1),
                    ('Rezervacija', 2),
                    ('Spremno', 3),
                    ('Isporuka', 4)
            )
            select ordered_phases.phase_name as "phaseName",
                   count(distinct proc.id) as "orderCount"
            from ordered_phases
            left join phase_type phase_type on phase_type.name = ordered_phases.phase_name
            left join phase phase on phase.type_id = phase_type.id
                and phase.end_time is null
            left join processes proc on proc.id = phase.process_id
                and proc.end_time is null
            group by ordered_phases.phase_name, ordered_phases.sort_order
            order by ordered_phases.sort_order
            """, nativeQuery = true)
    List<PhaseCountView> findActivePhaseCounts();

    @Query(value = """
            with phase_pairs(from_phase, to_phase, sort_order) as (
                values
                    ('Ponuda', 'Rezervacija', 1),
                    ('Rezervacija', 'Spremno', 2),
                    ('Spremno', 'Isporuka', 3)
            ),
            reached as (
                select phase_pairs.from_phase,
                       count(distinct phase.process_id) as reached_count
                from phase_pairs
                left join phase_type phase_type on phase_type.name = phase_pairs.from_phase
                left join phase phase on phase.type_id = phase_type.id
                group by phase_pairs.from_phase
            ),
            transitioned as (
                select phase_pairs.from_phase,
                       phase_pairs.to_phase,
                       count(distinct from_phase.process_id) as transitioned_count
                from phase_pairs
                join phase_type from_type on from_type.name = phase_pairs.from_phase
                join phase from_phase on from_phase.type_id = from_type.id
                join phase_type to_type on to_type.name = phase_pairs.to_phase
                join phase to_phase on to_phase.process_id = from_phase.process_id
                    and to_phase.type_id = to_type.id
                    and to_phase.start_time >= from_phase.start_time
                group by phase_pairs.from_phase, phase_pairs.to_phase
            )
            select phase_pairs.from_phase as "fromPhase",
                   phase_pairs.to_phase as "toPhase",
                   coalesce(reached.reached_count, 0) as "reachedCount",
                   coalesce(transitioned.transitioned_count, 0) as "transitionedCount",
                   case
                       when coalesce(reached.reached_count, 0) = 0 then 0
                       else coalesce(transitioned.transitioned_count, 0)::double precision * 100 / reached.reached_count
                   end as "ratePercent"
            from phase_pairs
            left join reached on reached.from_phase = phase_pairs.from_phase
            left join transitioned on transitioned.from_phase = phase_pairs.from_phase
                and transitioned.to_phase = phase_pairs.to_phase
            order by phase_pairs.sort_order
            """, nativeQuery = true)
    List<TransitionRateView> findTransitionRates();

    @Query(value = """
            with ordered_phases(phase_name, sort_order) as (
                values
                    ('Ponuda', 1),
                    ('Rezervacija', 2),
                    ('Spremno', 3),
                    ('Isporuka', 4)
            )
            select ordered_phases.phase_name as "phaseName",
                   coalesce(avg(extract(epoch from (phase.end_time - phase.start_time)))::bigint, 0) as "averageDurationSeconds",
                   count(phase.id) as "sampleCount"
            from ordered_phases
            left join phase_type phase_type on phase_type.name = ordered_phases.phase_name
            left join phase phase on phase.type_id = phase_type.id
                and phase.start_time is not null
                and phase.end_time is not null
            group by ordered_phases.phase_name, ordered_phases.sort_order
            order by ordered_phases.sort_order
            """, nativeQuery = true)
    List<AverageDurationView> findAveragePhaseDurations();

    @Query(value = """
            with active_orders as (
                select phase_type.name as phase_name,
                       row_number() over (
                           partition by phase_type.name
                           order by extract(epoch from (now() - phase.start_time)) desc, proc.id asc
                       ) as phase_rank,
                       proc.id as process_id,
                       phase.offer_id as offer_id,
                       proc.customer_id as customer_id,
                       trim(concat(coalesce(account.first_name, ''), ' ', coalesce(account.last_name, ''))) as customer_name,
                       account.email as customer_email,
                       phase.start_time as phase_start_time,
                       extract(epoch from (now() - phase.start_time))::bigint as duration_seconds
                from phase
                join phase_type on phase.type_id = phase_type.id
                join processes proc on proc.id = phase.process_id
                join account on account.id = proc.customer_id
                where phase.end_time is null
                    and proc.end_time is null
                    and phase_type.name in ('Ponuda', 'Rezervacija', 'Spremno', 'Isporuka')
            )
            select phase_name as "phaseName",
                   phase_rank as "phaseRank",
                   process_id as "processId",
                   offer_id as "offerId",
                   customer_id as "customerId",
                   customer_name as "customerName",
                   customer_email as "customerEmail",
                   phase_start_time as "phaseStartTime",
                   duration_seconds as "durationSeconds"
            from active_orders
            where phase_rank <= 3
            order by
                case phase_name
                    when 'Ponuda' then 1
                    when 'Rezervacija' then 2
                    when 'Spremno' then 3
                    when 'Isporuka' then 4
                    else 5
                end,
                phase_rank
            """, nativeQuery = true)
    List<LongestActiveOrderView> findLongestActiveOrdersByPhase();
}
