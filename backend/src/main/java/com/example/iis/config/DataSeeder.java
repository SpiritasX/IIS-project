package com.example.iis.config;

import com.example.iis.model.*;
import com.example.iis.repository.*;
import com.example.iis.service.NoSqlSyncSagaService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DataSeeder {
    private static final List<CatalogSeed> CATALOG_SEEDS = List.of(
            seed("Flowers", "Flowering plants", "Lavender", "English lavender", 45.0, "Well-drained alkaline soil", "Keep in full sun and water sparingly.", "SUMMER", "Lavender starter", "Hardy young lavender plant with rich fragrance and strong roots.", "Cuttings", "Available", "1000"),
            seed("Flowers", "Flowering plants", "Lavender", "French lavender", 42.0, "Sandy alkaline soil", "Provide bright sun and prune after flowering.", "SUMMER", "French lavender pot", "Compact lavender with ornamental bracts and silver-green foliage.", "Cuttings", "Available", "1150"),
            seed("Flowers", "Flowering plants", "Rose", "Garden rose", 55.0, "Loamy soil", "Prune spent blooms and water at the base.", "SUMMER", "Rose bush", "Classic rose bush with seasonal blooms and balanced growth.", "Cuttings", "Available", "1400"),
            seed("Flowers", "Flowering plants", "Rose", "Climbing rose", 58.0, "Deep fertile loam", "Train canes on support and feed during active growth.", "SUMMER", "Climbing rose cane", "Vigorous rose for arches, fences, and sunny garden walls.", "Cuttings", "Available", "2100"),
            seed("Flowers", "Flowering plants", "Hydrangea", "Bigleaf hydrangea", 70.0, "Moist acidic soil", "Keep soil evenly moist and protect from afternoon heat.", "SUMMER", "Hydrangea nursery shrub", "Rounded hydrangea with lush foliage and large summer flower heads.", "Cuttings", "Low stock", "1900"),
            seed("Flowers", "Flowering plants", "Marigold", "French marigold", 50.0, "Well-drained garden soil", "Deadhead flowers to extend blooming.", "SUMMER", "Marigold tray", "Bright marigold seedlings for borders, planters, and vegetable beds.", "Seed", "Available", "520"),
            seed("Flowers", "Flowering plants", "Petunia", "Grandiflora petunia", 50.0, "Light fertile soil", "Pinch early growth and water when the topsoil dries.", "SUMMER", "Petunia hanging pot", "Colorful trailing petunia suited for balconies and sunny patios.", "Seed", "Available", "780"),
            seed("Flowers", "Flowering plants", "Chrysanthemum", "Garden mum", 55.0, "Rich well-drained soil", "Keep evenly watered and pinch tips before buds form.", "AUTUMN", "Chrysanthemum pot", "Dense autumn-flowering plant with long-lasting blooms.", "Cuttings", "Available", "900"),
            seed("Flowers", "Flowering plants", "Begonia", "Wax begonia", 60.0, "Moist humus-rich soil", "Grow in filtered light and avoid wet leaves overnight.", "SUMMER", "Begonia six-pack", "Compact bedding begonia with glossy leaves and steady flowers.", "Cuttings", "Available", "690"),
            seed("Flowers", "Bulbs", "Tulip", "Darwin hybrid tulip", 45.0, "Loose well-drained soil", "Plant bulbs deeply in autumn and keep cool through winter.", "SPRING", "Tulip bulb pack", "Spring tulip bulbs with strong stems and large cup-shaped blooms.", "Bulb", "Available", "850"),
            seed("Flowers", "Bulbs", "Daffodil", "Trumpet daffodil", 45.0, "Well-drained neutral soil", "Plant in autumn and allow foliage to yellow naturally.", "SPRING", "Daffodil bulb pack", "Reliable spring bulbs with bright trumpet flowers.", "Bulb", "Available", "790"),
            seed("Flowers", "Bulbs", "Lily", "Asiatic lily", 50.0, "Fertile well-drained soil", "Keep roots cool and provide sun for strong flowering.", "SUMMER", "Asiatic lily bulb", "Hardy lily bulb producing upright stems and bold summer flowers.", "Bulb", "Available", "980"),
            seed("Flowers", "Bulbs", "Iris", "Bearded iris", 40.0, "Slightly alkaline well-drained soil", "Keep rhizomes near the surface and divide crowded clumps.", "SPRING", "Bearded iris rhizome", "Low-maintenance iris with sword leaves and showy spring flowers.", "Division", "Available", "740"),
            seed("Flowers", "Bulbs", "Dahlia", "Decorative dahlia", 55.0, "Rich loose soil", "Stake tall stems and lift tubers before hard frost.", "AUTUMN", "Dahlia tuber", "Decorative dahlia tuber for large late-season blooms.", "Tuber", "Low stock", "1200"),

            seed("Herbs", "Culinary herbs", "Basil", "Genovese basil", 60.0, "Rich, moist soil", "Pinch top leaves often to encourage growth.", "WINTER", "Basil seedling", "Fresh culinary basil seedling ready for a sunny kitchen window.", "Seed", "Available", "750"),
            seed("Herbs", "Culinary herbs", "Basil", "Thai basil", 58.0, "Fertile moist soil", "Harvest frequently and keep above cool drafts.", "SUMMER", "Thai basil pot", "Aromatic basil with purple stems and a warm anise note.", "Seed", "Available", "820"),
            seed("Herbs", "Culinary herbs", "Mint", "Spearmint", 65.0, "Moist garden soil", "Trim runners and keep soil evenly moist.", "WINTER", "Mint pot", "Fast-growing mint in a compact nursery pot for easy transplanting.", "Division", "Available", "650"),
            seed("Herbs", "Culinary herbs", "Mint", "Peppermint", 68.0, "Moist rich soil", "Grow in containers to manage spreading roots.", "SUMMER", "Peppermint pot", "Strongly scented mint for teas, desserts, and herb planters.", "Division", "Available", "690"),
            seed("Herbs", "Culinary herbs", "Rosemary", "Tuscan blue rosemary", 40.0, "Sandy well-drained soil", "Give full sun and let soil dry between waterings.", "SUMMER", "Rosemary bush", "Upright rosemary with fragrant needles and sturdy woody stems.", "Cuttings", "Available", "980"),
            seed("Herbs", "Culinary herbs", "Thyme", "Common thyme", 38.0, "Dry gritty soil", "Trim lightly after flowering and avoid heavy watering.", "SUMMER", "Thyme plug tray", "Compact thyme plugs for herb gardens, edging, and pots.", "Cuttings", "Available", "620"),
            seed("Herbs", "Culinary herbs", "Oregano", "Greek oregano", 45.0, "Lean well-drained soil", "Harvest before flowering for best flavor.", "SUMMER", "Oregano pot", "Robust oregano with dense leaves and a strong savory aroma.", "Division", "Available", "700"),
            seed("Herbs", "Culinary herbs", "Parsley", "Flat-leaf parsley", 60.0, "Moist fertile soil", "Harvest outer stems and keep evenly watered.", "SPRING", "Parsley seedling tray", "Flat-leaf parsley seedlings for kitchen gardens and planters.", "Seed", "Available", "560"),
            seed("Herbs", "Culinary herbs", "Cilantro", "Leisure cilantro", 58.0, "Cool moist soil", "Sow successively and harvest before plants bolt.", "SPRING", "Cilantro seedling tray", "Cool-season cilantro seedlings with tender fragrant leaves.", "Seed", "Available", "540"),
            seed("Herbs", "Medicinal herbs", "Sage", "Common sage", 42.0, "Dry well-drained soil", "Prune woody stems lightly and keep in full sun.", "SUMMER", "Sage pot", "Silver-leaved sage plant suited for herb beds and containers.", "Cuttings", "Available", "760"),
            seed("Herbs", "Medicinal herbs", "Chamomile", "German chamomile", 50.0, "Light sandy soil", "Harvest flowers when fully open and keep planting airy.", "SUMMER", "Chamomile seedling", "Fine-leaved chamomile for herbal tea gardens and pollinator beds.", "Seed", "Available", "580"),
            seed("Herbs", "Medicinal herbs", "Lemon balm", "Common lemon balm", 60.0, "Moist loamy soil", "Cut back regularly to keep growth compact.", "SUMMER", "Lemon balm pot", "Lemon-scented herb with soft leaves for tea and borders.", "Division", "Available", "680"),
            seed("Herbs", "Medicinal herbs", "Echinacea", "Purple coneflower", 45.0, "Average well-drained soil", "Deadhead for repeat bloom or leave seed heads in autumn.", "SUMMER", "Echinacea plug", "Hardy medicinal perennial with purple daisy-like flowers.", "Seed", "Available", "880"),

            seed("Trees", "Fruit trees", "Olive", "Arbequina olive", 40.0, "Sandy loam", "Place in a warm bright spot and avoid overwatering.", "SUMMER", "Olive sapling", "Mediterranean olive sapling suited for patios and warm gardens.", "Grafting", "Available", "1800"),
            seed("Trees", "Fruit trees", "Apple", "Gala apple", 50.0, "Deep loamy soil", "Plant with a pollination partner and prune in dormancy.", "AUTUMN", "Gala apple sapling", "Young apple tree with sweet fruit potential for home orchards.", "Grafting", "Available", "2400"),
            seed("Trees", "Fruit trees", "Pear", "Williams pear", 50.0, "Moist well-drained loam", "Thin fruit early and prune to an open shape.", "AUTUMN", "Williams pear sapling", "Grafted pear sapling for sunny garden orchard rows.", "Grafting", "Available", "2500"),
            seed("Trees", "Fruit trees", "Cherry", "Stella cherry", 48.0, "Fertile well-drained soil", "Protect young fruit from birds and prune after harvest.", "SUMMER", "Stella cherry sapling", "Self-fertile cherry sapling with glossy leaves and spring bloom.", "Grafting", "Low stock", "2700"),
            seed("Trees", "Fruit trees", "Fig", "Brown turkey fig", 45.0, "Free-draining loam", "Grow in full sun and restrict roots for better fruiting.", "SUMMER", "Fig tree pot", "Hardy fig tree in a nursery pot for warm sheltered spots.", "Cuttings", "Available", "2200"),
            seed("Trees", "Fruit trees", "Peach", "Redhaven peach", 48.0, "Fertile sandy loam", "Thin fruit and keep foliage airy to reduce disease.", "SUMMER", "Redhaven peach sapling", "Vigorous peach sapling for sunny, protected orchard sites.", "Grafting", "Available", "2600"),
            seed("Trees", "Fruit trees", "Plum", "Stanley plum", 50.0, "Moist well-drained soil", "Prune lightly and water during fruit swelling.", "AUTUMN", "Stanley plum sapling", "Grafted plum sapling with reliable late-season fruiting.", "Grafting", "Available", "2450"),
            seed("Trees", "Ornamental trees", "Japanese maple", "Bloodgood maple", 55.0, "Moist slightly acidic soil", "Shelter from hot afternoon sun and drying wind.", "SPRING", "Japanese maple pot", "Compact ornamental maple with finely cut red foliage.", "Grafting", "Available", "3200"),
            seed("Trees", "Ornamental trees", "Magnolia", "Saucer magnolia", 55.0, "Rich acidic loam", "Plant in a sheltered location and avoid root disturbance.", "SPRING", "Magnolia sapling", "Deciduous magnolia with large cup-shaped spring flowers.", "Cuttings", "Available", "3100"),
            seed("Trees", "Ornamental trees", "Birch", "Silver birch", 50.0, "Moist well-drained soil", "Water young trees deeply through dry periods.", "SPRING", "Silver birch sapling", "Slender birch sapling with pale bark and airy foliage.", "Seed", "Available", "1900"),
            seed("Trees", "Conifers", "Pine", "Austrian pine", 42.0, "Sandy well-drained soil", "Water deeply while establishing and provide full sun.", "WINTER", "Austrian pine sapling", "Hardy evergreen pine suited for windbreaks and open landscapes.", "Seed", "Available", "2100"),
            seed("Trees", "Conifers", "Spruce", "Blue spruce", 45.0, "Acidic well-drained soil", "Keep roots cool and avoid compacted soil.", "WINTER", "Blue spruce sapling", "Blue-toned evergreen spruce for structured landscape planting.", "Grafting", "Available", "2800"),
            seed("Trees", "Conifers", "Cypress", "Leyland cypress", 45.0, "Fertile well-drained soil", "Trim regularly if grown as a hedge.", "WINTER", "Leyland cypress liner", "Fast-growing evergreen liner for screens and boundary planting.", "Cuttings", "Available", "1600"),

            seed("Houseplants", "Indoor foliage", "Monstera", "Monstera deliciosa", 65.0, "Chunky aerated potting mix", "Give bright indirect light and support climbing stems.", "SUMMER", "Monstera young plant", "Popular indoor foliage plant with broad split leaves.", "Cuttings", "Available", "2200"),
            seed("Houseplants", "Indoor foliage", "Pothos", "Golden pothos", 55.0, "Well-drained houseplant mix", "Let the topsoil dry and trim vines to shape.", "WINTER", "Golden pothos basket", "Trailing pothos with marbled leaves for shelves and hanging pots.", "Cuttings", "Available", "1250"),
            seed("Houseplants", "Indoor foliage", "Philodendron", "Heartleaf philodendron", 60.0, "Peat-free indoor mix", "Grow in indirect light and avoid standing water.", "WINTER", "Heartleaf philodendron pot", "Easy-care vining philodendron with glossy heart-shaped leaves.", "Cuttings", "Available", "1180"),
            seed("Houseplants", "Indoor foliage", "Snake plant", "Laurentii snake plant", 35.0, "Fast-draining cactus mix", "Water sparingly and keep in moderate to bright light.", "WINTER", "Snake plant pot", "Upright low-maintenance houseplant with variegated sword leaves.", "Division", "Available", "1450"),
            seed("Houseplants", "Indoor foliage", "Calathea", "Rattlesnake calathea", 70.0, "Moist peat-free mix", "Use filtered water and keep away from direct sun.", "SUMMER", "Calathea pot", "Patterned foliage plant for humid indoor rooms.", "Division", "Available", "1750"),
            seed("Houseplants", "Indoor foliage", "Ficus", "Fiddle-leaf fig", 55.0, "Well-drained indoor potting mix", "Provide bright filtered light and rotate regularly.", "SUMMER", "Fiddle-leaf fig pot", "Tall indoor ficus with large violin-shaped leaves.", "Cuttings", "Low stock", "2900"),
            seed("Houseplants", "Indoor foliage", "Rubber plant", "Burgundy rubber plant", 50.0, "Loamy indoor mix", "Wipe leaves and let soil dry slightly between waterings.", "SUMMER", "Rubber plant pot", "Glossy dark-leaved rubber plant for bright indoor spaces.", "Cuttings", "Available", "2100"),
            seed("Houseplants", "Indoor flowering", "Peace lily", "Spathiphyllum wallisii", 65.0, "Moist rich potting mix", "Keep in medium light and water when leaves soften.", "SPRING", "Peace lily pot", "Indoor flowering plant with dark leaves and white spathes.", "Division", "Available", "1600"),
            seed("Houseplants", "Indoor flowering", "African violet", "Saintpaulia ionantha", 60.0, "Light African violet mix", "Water from below and keep in bright indirect light.", "SPRING", "African violet pot", "Compact flowering houseplant with velvety leaves.", "Leaf cuttings", "Available", "980"),
            seed("Houseplants", "Indoor flowering", "Orchid", "Phalaenopsis orchid", 60.0, "Coarse orchid bark", "Water when bark dries and keep crown dry.", "WINTER", "Phalaenopsis orchid", "Elegant moth orchid with long-lasting blooms.", "Division", "Available", "2400"),
            seed("Houseplants", "Indoor ferns", "Boston fern", "Nephrolepis exaltata", 75.0, "Moist humus-rich mix", "Keep humid and trim old fronds at the base.", "SUMMER", "Boston fern basket", "Arching fern basket for bright humid interiors.", "Division", "Available", "1350"),
            seed("Houseplants", "Indoor ferns", "Maidenhair fern", "Adiantum raddianum", 78.0, "Evenly moist rich mix", "Keep consistently moist and away from dry air.", "SUMMER", "Maidenhair fern pot", "Delicate fern with fine stems and soft fan-shaped leaflets.", "Division", "Low stock", "1550"),

            seed("Vegetables", "Leafy vegetables", "Lettuce", "Butterhead lettuce", 65.0, "Cool fertile soil", "Harvest outer leaves and keep soil moist.", "SPRING", "Butterhead lettuce tray", "Tender lettuce seedlings for cool-season salad beds.", "Seed", "Available", "430"),
            seed("Vegetables", "Leafy vegetables", "Spinach", "Bloomsdale spinach", 65.0, "Moist nitrogen-rich soil", "Grow in cool weather and harvest young leaves.", "SPRING", "Spinach seedling tray", "Savoyed spinach seedlings for quick garden harvests.", "Seed", "Available", "450"),
            seed("Vegetables", "Leafy vegetables", "Kale", "Lacinato kale", 60.0, "Rich well-drained soil", "Pick lower leaves regularly and feed during growth.", "AUTUMN", "Lacinato kale six-pack", "Dark-leaved kale seedlings for autumn and winter harvests.", "Seed", "Available", "520"),
            seed("Vegetables", "Fruiting vegetables", "Tomato", "Roma tomato", 60.0, "Fertile warm soil", "Stake plants and water consistently at the root zone.", "SUMMER", "Roma tomato seedling", "Paste tomato seedling for sauces, raised beds, and pots.", "Seed", "Available", "620"),
            seed("Vegetables", "Fruiting vegetables", "Tomato", "Cherry tomato", 58.0, "Rich warm soil", "Provide support and harvest ripe clusters often.", "SUMMER", "Cherry tomato seedling", "Productive cherry tomato seedling with sweet bite-size fruit.", "Seed", "Available", "690"),
            seed("Vegetables", "Fruiting vegetables", "Pepper", "California wonder pepper", 55.0, "Warm fertile soil", "Plant after nights are warm and feed lightly.", "SUMMER", "Bell pepper seedling", "Sweet bell pepper seedling for sunny vegetable beds.", "Seed", "Available", "610"),
            seed("Vegetables", "Fruiting vegetables", "Eggplant", "Black beauty eggplant", 58.0, "Warm rich soil", "Mulch after soil warms and keep evenly watered.", "SUMMER", "Eggplant seedling", "Classic eggplant seedling with glossy dark fruit potential.", "Seed", "Available", "640"),
            seed("Vegetables", "Fruiting vegetables", "Cucumber", "Marketmore cucumber", 65.0, "Moist fertile soil", "Train vines and harvest before fruit becomes oversized.", "SUMMER", "Cucumber seedling", "Vining cucumber seedling for trellises and garden beds.", "Seed", "Available", "570"),
            seed("Vegetables", "Root vegetables", "Carrot", "Nantes carrot", 55.0, "Loose sandy soil", "Keep soil evenly moist until germination and thin seedlings.", "SPRING", "Carrot seed packet", "Nantes carrot seed packet for sweet cylindrical roots.", "Seed", "Available", "330"),
            seed("Vegetables", "Root vegetables", "Beet", "Detroit dark red beet", 58.0, "Loose fertile soil", "Thin seedlings and keep beds evenly moist.", "SPRING", "Beet seed packet", "Reliable beet seed for tender greens and rounded roots.", "Seed", "Available", "350"),
            seed("Vegetables", "Root vegetables", "Radish", "Cherry belle radish", 55.0, "Loose cool soil", "Sow in short rows and harvest roots young.", "SPRING", "Radish seed packet", "Fast-growing radish seed for crisp spring harvests.", "Seed", "Available", "280"),
            seed("Vegetables", "Brassicas", "Broccoli", "Calabrese broccoli", 60.0, "Rich firm soil", "Feed steadily and harvest central heads before flowers open.", "AUTUMN", "Broccoli seedling tray", "Calabrese broccoli seedlings for cool-season planting.", "Seed", "Available", "590"),

            seed("Succulents", "Cacti", "Barrel cactus", "Golden barrel cactus", 25.0, "Mineral cactus mix", "Provide strong light and water only when mix is dry.", "SUMMER", "Golden barrel cactus", "Round cactus with golden spines for sunny windowsills.", "Seed", "Available", "1350"),
            seed("Succulents", "Cacti", "Prickly pear", "Eastern prickly pear", 28.0, "Sandy gritty soil", "Grow in full sun and keep dry in winter.", "SUMMER", "Prickly pear pad", "Hardy cactus pad ready for dry gardens or containers.", "Cuttings", "Available", "780"),
            seed("Succulents", "Cacti", "Holiday cactus", "Christmas cactus", 45.0, "Loose well-drained mix", "Keep slightly drier before bud set and avoid cold drafts.", "WINTER", "Christmas cactus pot", "Segmented cactus known for colorful winter flowers.", "Cuttings", "Available", "1100"),
            seed("Succulents", "Rosette succulents", "Echeveria", "Echeveria elegans", 30.0, "Fast-draining succulent mix", "Give bright light and water around the rosette, not inside it.", "SUMMER", "Echeveria rosette", "Compact blue-green rosette succulent for small pots.", "Leaf cuttings", "Available", "720"),
            seed("Succulents", "Rosette succulents", "Aloe", "Aloe vera", 35.0, "Sandy succulent mix", "Allow soil to dry fully and remove offsets when crowded.", "SUMMER", "Aloe vera pot", "Useful aloe plant with fleshy upright leaves.", "Division", "Available", "950"),
            seed("Succulents", "Rosette succulents", "Haworthia", "Zebra haworthia", 35.0, "Gritty succulent mix", "Keep in bright indirect light and avoid overwatering.", "WINTER", "Zebra haworthia pot", "Small striped succulent suited for desks and windowsills.", "Division", "Available", "760"),
            seed("Succulents", "Trailing succulents", "Sedum", "Burro's tail", 32.0, "Fast-draining gritty mix", "Handle gently and water after soil dries.", "SUMMER", "Burro's tail basket", "Trailing succulent with bead-like leaves for hanging planters.", "Cuttings", "Available", "1250"),
            seed("Succulents", "Trailing succulents", "String of pearls", "Senecio rowleyanus", 35.0, "Loose cactus mix", "Give bright light and water lightly from the soil surface.", "SUMMER", "String of pearls pot", "Trailing succulent with round pearl-like leaves.", "Cuttings", "Low stock", "1450"),

            seed("Shrubs", "Evergreen shrubs", "Boxwood", "Common boxwood", 45.0, "Well-drained alkaline soil", "Trim after new growth hardens and water during establishment.", "SPRING", "Boxwood shrub", "Dense evergreen shrub for hedges, edging, and containers.", "Cuttings", "Available", "1300"),
            seed("Shrubs", "Evergreen shrubs", "Azalea", "Kurume azalea", 60.0, "Acidic humus-rich soil", "Keep roots cool and mulch with acidic organic matter.", "SPRING", "Azalea shrub", "Spring-flowering evergreen shrub with vivid clustered blooms.", "Cuttings", "Available", "1700"),
            seed("Shrubs", "Evergreen shrubs", "Camellia", "Japanese camellia", 62.0, "Acidic well-drained soil", "Shelter from morning sun after frost and keep evenly moist.", "WINTER", "Camellia shrub", "Glossy evergreen shrub with elegant cool-season flowers.", "Cuttings", "Low stock", "2400"),
            seed("Shrubs", "Berry shrubs", "Blueberry", "Duke blueberry", 60.0, "Acidic peat-rich soil", "Use acidic mulch and keep soil moist during fruiting.", "SUMMER", "Blueberry bush", "Early-season blueberry bush for containers or acidic garden beds.", "Cuttings", "Available", "1900"),
            seed("Shrubs", "Berry shrubs", "Raspberry", "Heritage raspberry", 55.0, "Fertile well-drained soil", "Tie canes to support and remove spent canes after harvest.", "SUMMER", "Raspberry cane bundle", "Ever-bearing raspberry canes for home berry patches.", "Division", "Available", "1500"),
            seed("Shrubs", "Berry shrubs", "Blackberry", "Thornless blackberry", 55.0, "Deep fertile soil", "Train canes on wires and prune after fruiting.", "SUMMER", "Thornless blackberry cane", "Thornless blackberry for productive trellised garden rows.", "Cuttings", "Available", "1650"),
            seed("Shrubs", "Flowering shrubs", "Forsythia", "Border forsythia", 48.0, "Average well-drained soil", "Prune just after flowering to maintain shape.", "SPRING", "Forsythia shrub", "Early spring shrub with bright yellow blooms.", "Cuttings", "Available", "1250"),
            seed("Shrubs", "Flowering shrubs", "Spirea", "Goldflame spirea", 50.0, "Average garden soil", "Shear lightly after bloom and water during dry spells.", "SUMMER", "Spirea shrub", "Compact spirea with colorful foliage and pink flower clusters.", "Cuttings", "Available", "1320"),

            seed("Vines", "Climbing vines", "Clematis", "Jackmanii clematis", 55.0, "Cool moist loam", "Shade the roots and guide stems onto support.", "SUMMER", "Clematis vine", "Flowering clematis vine for trellises, pergolas, and fences.", "Cuttings", "Available", "1800"),
            seed("Vines", "Climbing vines", "Grape", "Concord grape", 50.0, "Deep well-drained loam", "Train to a wire support and prune hard in winter.", "AUTUMN", "Concord grape vine", "Hardy grape vine for arbors and small vineyard rows.", "Cuttings", "Available", "2100"),
            seed("Vines", "Climbing vines", "Jasmine", "Star jasmine", 55.0, "Fertile well-drained soil", "Provide support and protect young plants from harsh frost.", "SUMMER", "Star jasmine pot", "Fragrant evergreen climber for warm patios and trellises.", "Cuttings", "Available", "1750"),
            seed("Vines", "Climbing vines", "Wisteria", "Chinese wisteria", 50.0, "Fertile well-drained soil", "Train onto strong support and prune twice a year.", "SPRING", "Wisteria vine", "Vigorous flowering vine with cascading spring bloom clusters.", "Grafting", "Available", "2300"),
            seed("Vines", "Edible vines", "Kiwi", "Hardy kiwi", 55.0, "Moist fertile soil", "Plant with a pollination partner and train on a sturdy trellis.", "AUTUMN", "Hardy kiwi vine", "Cold-tolerant kiwi vine with small smooth-skinned fruit potential.", "Cuttings", "Available", "2400"),

            seed("Grasses", "Ornamental grasses", "Fountain grass", "Hameln fountain grass", 40.0, "Well-drained average soil", "Cut back old growth before spring shoots emerge.", "AUTUMN", "Fountain grass clump", "Compact ornamental grass with soft bottlebrush plumes.", "Division", "Available", "980"),
            seed("Grasses", "Ornamental grasses", "Feather reed grass", "Karl Foerster grass", 42.0, "Moist well-drained soil", "Leave seed heads for winter structure and cut back in spring.", "SUMMER", "Feather reed grass pot", "Upright ornamental grass with vertical tan flower plumes.", "Division", "Available", "1150"),
            seed("Grasses", "Ornamental grasses", "Blue fescue", "Elijah blue fescue", 35.0, "Dry well-drained soil", "Comb out old blades and divide clumps when crowded.", "SPRING", "Blue fescue pot", "Small blue ornamental grass for borders and rock gardens.", "Division", "Available", "780"),
            seed("Grasses", "Lawn grasses", "Ryegrass", "Perennial ryegrass", 50.0, "Fertile well-drained soil", "Keep seedbed moist until established and mow regularly.", "SPRING", "Ryegrass seed bag", "Perennial ryegrass seed blend for quick lawn establishment.", "Seed", "Available", "1350"),

            seed("Aquatic Plants", "Pond plants", "Water lily", "Hardy water lily", 80.0, "Aquatic loam", "Set rhizome in a pond basket and place below water surface.", "SUMMER", "Hardy water lily rhizome", "Pond water lily rhizome with floating leaves and summer flowers.", "Division", "Available", "2200"),
            seed("Aquatic Plants", "Pond plants", "Lotus", "Sacred lotus", 85.0, "Heavy aquatic soil", "Grow in a wide container and keep water warm.", "SUMMER", "Lotus tuber", "Sacred lotus tuber for sunny ornamental ponds.", "Tuber", "Low stock", "2600"),
            seed("Aquatic Plants", "Marginal plants", "Yellow iris", "Yellow flag iris", 80.0, "Wet marginal soil", "Plant at pond edges and divide clumps as needed.", "SPRING", "Yellow flag iris pot", "Moisture-loving iris for pond margins and rain gardens.", "Division", "Available", "950"),
            seed("Aquatic Plants", "Marginal plants", "Papyrus", "Dwarf papyrus", 82.0, "Wet rich soil", "Keep constantly moist and protect from frost.", "SUMMER", "Dwarf papyrus pot", "Architectural marginal plant with umbrella-like stems.", "Division", "Available", "1250")
    );

    @Bean
    CommandLineRunner seedData(
            AdminRepository adminRepository,
            OfferStatusRepository offerStatusRepository,
            PhaseTypeRepository phaseTypeRepository,
            PlantCategoryRepository plantCategoryRepository,
            PlantTypeRepository plantTypeRepository,
            PlantSpeciesRepository plantSpeciesRepository,
            PlantVarietyRepository plantVarietyRepository,
            PlantRepository plantRepository,
            PlantPriceRepository plantPriceRepository,
            NoSqlSyncSagaService noSqlSyncSagaService
    ) {
        return args -> {
            seedOrderStatuses(offerStatusRepository);
            seedPhaseTypes(phaseTypeRepository);

            if (plantPriceRepository.count() > 0) {
                return;
            }

            if (adminRepository.count() == 0) {
                adminRepository.save(new Admin("admin", "admin", "Pera", "Peric", "admin@example.com"));
            }

            seedCatalog(
                    plantCategoryRepository,
                    plantTypeRepository,
                    plantSpeciesRepository,
                    plantVarietyRepository,
                    plantRepository,
                    plantPriceRepository,
                    noSqlSyncSagaService
            );
        };
    }

    private void seedCatalog(
            PlantCategoryRepository plantCategoryRepository,
            PlantTypeRepository plantTypeRepository,
            PlantSpeciesRepository plantSpeciesRepository,
            PlantVarietyRepository plantVarietyRepository,
            PlantRepository plantRepository,
            PlantPriceRepository plantPriceRepository,
            NoSqlSyncSagaService noSqlSyncSagaService
    ) {
        Map<String, PlantCategory> categories = new LinkedHashMap<>();
        Map<String, PlantType> types = new LinkedHashMap<>();
        Map<String, PlantSpecies> species = new LinkedHashMap<>();
        Map<String, PlantVariety> varieties = new LinkedHashMap<>();

        for (CatalogSeed seed : CATALOG_SEEDS) {
            PlantCategory category = categories.computeIfAbsent(
                    seed.category(),
                    name -> plantCategoryRepository.save(new PlantCategory(name))
            );
            PlantType type = types.computeIfAbsent(
                    seed.type(),
                    name -> plantTypeRepository.save(new PlantType(name, category))
            );
            PlantSpecies plantSpecies = species.computeIfAbsent(
                    seed.species(),
                    name -> plantSpeciesRepository.save(new PlantSpecies(name, type))
            );
            PlantVariety variety = varieties.computeIfAbsent(
                    seed.variety(),
                    name -> plantVarietyRepository.save(new PlantVariety(
                            name,
                            seed.humidity(),
                            seed.soil(),
                            seed.instructions(),
                            seed.season(),
                            plantSpecies
                    ))
            );

            savePlantWithPrice(
                    plantRepository,
                    plantPriceRepository,
                    noSqlSyncSagaService,
                    new Plant(
                            seed.plantName(),
                            seed.description(),
                            seed.propagationMethod(),
                            seed.status(),
                            variety
                    ),
                    new BigDecimal(seed.price())
            );
        }
    }

    private void seedOrderStatuses(OfferStatusRepository offerStatusRepository) {
        saveStatusIfMissing(offerStatusRepository, "Pending");
        saveStatusIfMissing(offerStatusRepository, "Delivered");
        saveStatusIfMissing(offerStatusRepository, "Cancelled");
    }

    private void seedPhaseTypes(PhaseTypeRepository phaseTypeRepository) {
        if (phaseTypeRepository.findByName("Order placed").isEmpty()) {
            phaseTypeRepository.save(new PhaseType("Order placed"));
        }
    }

    private void saveStatusIfMissing(OfferStatusRepository offerStatusRepository, String name) {
        if (offerStatusRepository.findByName(name).isEmpty()) {
            offerStatusRepository.save(new OfferStatus(name));
        }
    }

    private void savePlantWithPrice(
            PlantRepository plantRepository,
            PlantPriceRepository plantPriceRepository,
            NoSqlSyncSagaService noSqlSyncSagaService,
            Plant plant,
            BigDecimal price
    ) {
        Plant savedPlant = plantRepository.save(plant);
        PlantPrice savedPrice = plantPriceRepository.save(new PlantPrice(price, savedPlant));
        noSqlSyncSagaService.syncCatalogItemCreated(savedPlant, savedPrice);
    }

    private static CatalogSeed seed(
            String category,
            String type,
            String species,
            String variety,
            Double humidity,
            String soil,
            String instructions,
            String season,
            String plantName,
            String description,
            String propagationMethod,
            String status,
            String price
    ) {
        return new CatalogSeed(
                category,
                type,
                species,
                variety,
                humidity,
                soil,
                instructions,
                season,
                plantName,
                description,
                propagationMethod,
                status,
                price
        );
    }

    private record CatalogSeed(
            String category,
            String type,
            String species,
            String variety,
            Double humidity,
            String soil,
            String instructions,
            String season,
            String plantName,
            String description,
            String propagationMethod,
            String status,
            String price
    ) {
    }
}
