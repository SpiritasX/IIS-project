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
            with phase_names(phase_name) as (
                values
                    ('Ponuda'),
                    ('Rezervacija'),
                    ('Spremno'),
                    ('Isporuka')
            )
            select phase_names.phase_name as "phaseName",
                   count(distinct proc.id) as "orderCount"
            from phase_names
            left join phase_type phase_type on phase_type.name = phase_names.phase_name
            left join phase phase on phase.type_id = phase_type.id
                and phase.end_time is null
            left join processes proc on proc.id = phase.process_id
                and proc.end_time is null
            group by phase_names.phase_name
            """, nativeQuery = true)
    List<PhaseCountView> findActivePhaseCounts();

    @Query(value = """
        with phase_counts as (
            select phase_type.name as phase_name,
                   count(distinct phase.process_id) as phase_count
            from phase
            join phase_type on phase.type_id = phase_type.id
            where phase_type.name in ('Ponuda', 'Rezervacija', 'Spremno', 'Isporuka')
            group by phase_type.name
        ),
        counts as (
            select
                sum(case when phase_name = 'Ponuda' then phase_count else 0 end) as ponuda_count,
                sum(case when phase_name = 'Rezervacija' then phase_count else 0 end) as rezervacija_count,
                sum(case when phase_name = 'Spremno' then phase_count else 0 end) as spremno_count,
                sum(case when phase_name = 'Isporuka' then phase_count else 0 end) as isporuka_count
            from phase_counts
        )
        select 'Ponuda' as "fromPhase",
               'Rezervacija' as "toPhase",
               ponuda_count as "reachedCount",
               rezervacija_count as "transitionedCount",
               case
                   when ponuda_count = 0 then 0
                   else rezervacija_count::double precision * 100 / ponuda_count
               end as "ratePercent"
        from counts

        union all

        select 'Rezervacija' as "fromPhase",
               'Spremno' as "toPhase",
               rezervacija_count as "reachedCount",
               spremno_count as "transitionedCount",
               case
                   when rezervacija_count = 0 then 0
                   else spremno_count::double precision * 100 / rezervacija_count
               end as "ratePercent"
        from counts

        union all

        select 'Spremno' as "fromPhase",
               'Isporuka' as "toPhase",
               spremno_count as "reachedCount",
               isporuka_count as "transitionedCount",
               case
                   when spremno_count = 0 then 0
                   else isporuka_count::double precision * 100 / spremno_count
               end as "ratePercent"
        from counts
        """, nativeQuery = true)
    List<TransitionRateView> findTransitionRates();

    @Query(value = """
        with phase_names(phase_name) as (
            values
                ('Ponuda'),
                ('Rezervacija'),
                ('Spremno'),
                ('Isporuka')
        )
        select phase_names.phase_name as "phaseName",
               coalesce(avg(extract(epoch from (phase.end_time - phase.start_time)))::bigint, 0) as "averageDurationSeconds", -- extract epoch vraca broj sekundi
               count(phase.id) as "sampleCount"
        from phase_names
        left join phase_type phase_type on phase_type.name = phase_names.phase_name
        left join phase phase on phase.type_id = phase_type.id
            and phase.start_time is not null
            and phase.end_time is not null
        group by phase_names.phase_name
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
