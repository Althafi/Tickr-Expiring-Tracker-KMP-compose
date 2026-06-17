package com.project.tickr.domain.di

import com.project.tickr.core.util.DateTimeUtil
import com.project.tickr.core.util.DateTimeUtilImpl
import com.project.tickr.domain.error.ErrorMessageProvider
import com.project.tickr.domain.usecase.auth.GetCurrentUserIdUseCase
import com.project.tickr.domain.usecase.auth.LoginUseCase
import com.project.tickr.domain.usecase.auth.ObserveSessionUseCase
import com.project.tickr.domain.usecase.auth.RegisterUseCase
import com.project.tickr.domain.usecase.auth.ResetPasswordUseCase
import com.project.tickr.domain.usecase.auth.SignInUseCase
import com.project.tickr.domain.usecase.auth.SignOutUseCase
import com.project.tickr.domain.usecase.auth.SignUpUseCase
import com.project.tickr.domain.usecase.auth.ValidateEmailUseCase
import com.project.tickr.domain.usecase.auth.ValidateNameUseCase
import com.project.tickr.domain.usecase.auth.ValidatePasswordUseCase
import com.project.tickr.domain.usecase.category.CreateCategoryUseCase
import com.project.tickr.domain.usecase.category.DeleteCategoryUseCase
import com.project.tickr.domain.usecase.category.GetCategoriesUseCase
import com.project.tickr.domain.usecase.category.GetCategoriesByUserUseCase
import com.project.tickr.domain.usecase.category.GetCategoryUseCase
import com.project.tickr.domain.usecase.category.UpdateCategoryUseCase
import com.project.tickr.domain.usecase.item.CreateItemUseCase
import com.project.tickr.domain.usecase.item.DeleteItemUseCase
import com.project.tickr.domain.usecase.item.GetExpiringItemsUseCase
import com.project.tickr.domain.usecase.item.GetItemUseCase
import com.project.tickr.domain.usecase.item.GetItemsByUserUseCase
import com.project.tickr.domain.usecase.item.GetItemsUseCase
import com.project.tickr.domain.usecase.item.MarkItemConsumedUseCase
import com.project.tickr.domain.usecase.item.UpdateItemUseCase
import com.project.tickr.domain.usecase.item.UploadProductImageUseCase
import com.project.tickr.domain.usecase.notification.DeleteNotificationUseCase
import com.project.tickr.domain.usecase.notification.GetNotificationsByItemUseCase
import com.project.tickr.domain.usecase.notification.GetNotificationsUseCase
import com.project.tickr.domain.usecase.notification.ScheduleNotificationUseCase
import com.project.tickr.domain.usecase.profile.GetProfileUseCase
import com.project.tickr.domain.usecase.profile.UpsertProfileUseCase
import com.project.tickr.domain.usecase.onboarding.GetOnboardingSeenUseCase
import com.project.tickr.domain.usecase.onboarding.CompleteOnboardingUseCase
import com.project.tickr.domain.usecase.home.AddItemUseCase
import com.project.tickr.domain.usecase.home.GetCategoryConsumptionUseCase
import com.project.tickr.domain.usecase.home.GetExpiringItemsGroupedUseCase
import com.project.tickr.domain.usecase.home.GetWasteTrendUseCase
import com.project.tickr.domain.usecase.expiry.GetExpiryListItemsUseCase
import com.project.tickr.domain.usecase.expiry.GetConsumptionStatsUseCase
import com.project.tickr.domain.usecase.consumed.GetConsumedItemsUseCase
import org.koin.dsl.module

val domainModule = module {
    single<DateTimeUtil> { DateTimeUtilImpl() }
    single { ErrorMessageProvider() }

    factory { SignUpUseCase(get()) }
    factory { SignInUseCase(get()) }
    factory { SignOutUseCase(get()) }
    factory { ObserveSessionUseCase(get()) }
    factory { GetCurrentUserIdUseCase(get()) }
    factory { ResetPasswordUseCase(get()) }
    // Phase 3.5 Auth use cases
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { ValidateEmailUseCase() }
    factory { ValidatePasswordUseCase() }
    factory { ValidateNameUseCase() }
    factory { com.project.tickr.domain.usecase.auth.ChangePasswordUseCase(get()) }

    factory { GetProfileUseCase(get()) }
    factory { UpsertProfileUseCase(get()) }

    factory { GetCategoriesUseCase(get()) }
    factory { GetCategoriesByUserUseCase(get()) }
    factory { GetCategoryUseCase(get()) }
    factory { CreateCategoryUseCase(get()) }
    factory { UpdateCategoryUseCase(get()) }
    factory { DeleteCategoryUseCase(get()) }

    factory { GetItemsUseCase(get()) }
    factory { GetItemsByUserUseCase(get()) }
    factory { GetItemUseCase(get()) }
    factory { CreateItemUseCase(get()) }
    factory { UpdateItemUseCase(get()) }
    factory { DeleteItemUseCase(get()) }
    factory { MarkItemConsumedUseCase(get()) }
    factory { GetExpiringItemsUseCase(get(), get()) }
    factory { UploadProductImageUseCase(get()) }

    factory { GetNotificationsUseCase(get()) }
    factory { GetNotificationsByItemUseCase(get()) }
    factory { ScheduleNotificationUseCase(get(), get()) }
    factory { DeleteNotificationUseCase(get()) }

    factory { GetOnboardingSeenUseCase(get()) }
    factory { CompleteOnboardingUseCase(get()) }

    // Phase 3.5 Home use cases
    factory { GetCategoryConsumptionUseCase(get(), get()) }
    factory { GetWasteTrendUseCase(get(), get()) }
    factory { GetExpiringItemsGroupedUseCase(get(), get(), get()) }
    factory { AddItemUseCase(get(), get()) }

    // Phase 3.5 Expiry screen use cases
    factory { GetExpiryListItemsUseCase(get(), get(), get()) }
    factory { GetConsumptionStatsUseCase(get(), get()) }

    // Consumed items
    factory { GetConsumedItemsUseCase(get(), get(), get()) }
}
