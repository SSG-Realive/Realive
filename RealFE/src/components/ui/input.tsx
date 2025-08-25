import * as React from "react"

import { cn } from "@/lib/utils"

function Input({ className, type, ...props }: React.ComponentProps<"input">) {
  return (
    <input
      type={type}
      data-slot="input"
      className={cn(
        "file:text-foreground placeholder:text-gray-400 selection:bg-blue-500/20 selection:text-blue-900 dark:selection:bg-blue-400/20 dark:selection:text-blue-100",
        "flex h-12 w-full min-w-0 rounded-none border-2 border-gray-200 bg-white/80 backdrop-blur-sm px-4 py-3 text-base shadow-sm transition-all duration-200",
        "file:inline-flex file:h-8 file:border-0 file:bg-transparent file:text-sm file:font-medium",
        "disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50 md:text-sm",
        "focus-visible:border-blue-500 focus-visible:ring-2 focus-visible:ring-blue-500/20 focus-visible:ring-offset-2 focus-visible:ring-offset-white focus-visible:outline-none",
        "aria-invalid:border-red-500 aria-invalid:ring-red-500/20",
        "hover:border-gray-300 hover:shadow-md",
        "dark:bg-gray-900/80 dark:border-gray-700 dark:text-gray-100 dark:placeholder:text-gray-500",
        "dark:focus-visible:border-blue-400 dark:focus-visible:ring-blue-400/20 dark:focus-visible:ring-offset-gray-900",
        "dark:hover:border-gray-600 dark:aria-invalid:border-red-400 dark:aria-invalid:ring-red-400/20",
        className
      )}
      {...props}
    />
  )
}

export { Input }
