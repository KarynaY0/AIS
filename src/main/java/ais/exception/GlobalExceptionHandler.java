package ais.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Global Exception Handler
 * Handles exceptions across the entire application
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle IllegalArgumentException - typically validation errors
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex,
                                        RedirectAttributes redirectAttributes) {
        logger.warn("Validation error: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/error";
    }

    /**
     * Handle ClassCastException - type casting errors
     */
    @ExceptionHandler(ClassCastException.class)
    public String handleClassCastException(ClassCastException ex,
                                           RedirectAttributes redirectAttributes) {
        logger.error("Type casting error: {}", ex.getMessage(), ex);
        redirectAttributes.addFlashAttribute("errorMessage",
                "An internal error occurred. Please try logging in again.");
        return "redirect:/error";
    }

    /**
     * Handle generic exceptions - catch-all for unexpected errors
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage",
                "An unexpected error occurred. Please contact support if the problem persists.");
        return "error";
    }



}