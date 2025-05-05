package com.tundynamcorp.flippingmedicalsuppliesassistant.data

enum class UserRole {
    Guest,
    User,         // just registered, in free trial
    Subscriber,   // has active subscription
    Admin         // special elevated privileges
}
