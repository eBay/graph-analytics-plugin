import org.gradle.api.attributes.Category

plugins {
    java
    id("com.ebay.graph-analytics")
}

// Sibling consumable configuration that is a real variant (has attributes) but does
// not declare graph-analytics capabilities. Selection must not pick this or the
// default jar over the requested graph-analytics capability.
configurations.consumable("unrelatedSiblingExport") {
    attributes.attribute(
        Category.CATEGORY_ATTRIBUTE,
        objects.named(Category::class.java, "unrelated-sibling"),
    )
}

dependencies {
    // PRODUCTION DEPENDENCIES PLACEHOLDER
    // TEST DEPENDENCIES PLACEHOLDER
}