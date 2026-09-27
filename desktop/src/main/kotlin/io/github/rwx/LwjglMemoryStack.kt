package io.github.rwx

import org.lwjgl.system.Configuration
import org.lwjgl.system.MemoryStack

/**
 * LWJGL sizes its per-thread [MemoryStack] exactly once, when the class initializes, by reading
 * [Configuration.STACK_SIZE]. Our startup opens the OpenAL device early and `ALC.create()` already
 * pushes a stack frame - so the size must be claimed before that, otherwise it locks at LWJGL's
 * 64 KB default and later requests are silent no-ops.
 *
 * That matters because game rendering enumerates native resources into single stack allocations
 * (extension lists, device info). At 64 KB only ~251 entries of ~260 bytes fit; GPU drivers that
 * expose more kill startup with `OutOfMemoryError: Out of stack space.` before the first frame.
 */
internal fun configureLwjglMemoryStack(sizeKb: Int = LWJGL_STACK_SIZE_KB) {
    System.setProperty(LWJGL_STACK_SIZE_PROPERTY, sizeKb.toString())
    Configuration.STACK_SIZE.set(sizeKb)
    MemoryStack.stackGet()
}

/** Roughly 2000 device extensions of headroom, against the ~251 that 64 KB allowed. */
internal const val LWJGL_STACK_SIZE_KB: Int = 512

private const val LWJGL_STACK_SIZE_PROPERTY: String = "org.lwjgl.system.stackSize"
