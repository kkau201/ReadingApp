package com.example.readingapp.testing

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import io.mockk.every
import io.mockk.mockk

/**
 * A finished [Task] whose listeners run straight away. Real tasks post listeners to the Android
 * main thread, which local unit tests don't have, so callback-style code would never continue.
 * Like Firebase, a failed task calls both its complete and failure listeners.
 */
fun <T> immediateTask(result: T? = null, error: Exception? = null): Task<T> =
    mockk<Task<T>>().also { task ->
        every { task.isSuccessful } returns (error == null)
        every { task.exception } returns error
        every { task.addOnSuccessListener(any()) } answers {
            @Suppress("UNCHECKED_CAST")
            if (error == null) (firstArg<OnSuccessListener<T>>()).onSuccess(result as T)
            task
        }
        every { task.addOnFailureListener(any()) } answers {
            error?.let { firstArg<OnFailureListener>().onFailure(it) }
            task
        }
        every { task.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<T>>().onComplete(task)
            task
        }
    }
