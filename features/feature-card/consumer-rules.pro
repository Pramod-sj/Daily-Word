# Keep data classes used for JSON parsing (Retrofit/Gson)
-keepclassmembers class com.pramod.dialyword.games.featureCard.FeatureCardResponse { *; }
-keepclassmembers class com.pramod.dialyword.games.featureCard.FeatureCard { *; }
-keepclassmembers class com.pramod.dialyword.games.featureCard.CardContent { *; }
-keepclassmembers class com.pramod.dialyword.games.featureCard.CardVisuals { *; }
-keepclassmembers class com.pramod.dialyword.games.featureCard.CardAction { *; }
-keepclassmembers class com.pramod.dialyword.games.featureCard.DismissConfig { *; }
-keepclassmembers class com.pramod.dialyword.games.featureCard.CardPlacement { *; }
-keepclassmembers class com.pramod.dialyword.games.featureCard.CardAnimation { *; }

# Alternatively, keep all data classes in this package if they are used for serialization
-keep class com.pramod.dialyword.games.featureCard.** { *; }
