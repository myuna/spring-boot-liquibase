package sample.liquibase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiOutput.Enabled;

public class OutputCapture implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, ParameterResolver, CharSequence {
    private OutputCapture.CaptureOutputStream captureOut;
    private OutputCapture.CaptureOutputStream captureErr;
    private ByteArrayOutputStream methodLevelCopy;
    private ByteArrayOutputStream classLevelCopy;
    private List<Matcher<? super String>> matchers = new ArrayList();

    public OutputCapture() {
    }

    public void afterEach(ExtensionContext context) {
        try {
            if (!this.matchers.isEmpty()) {
                String output = this.toString();
                Assert.assertThat(output, Matchers.allOf(this.matchers));
            }
        } finally {
            this.releaseOutput();
        }

    }

    public void beforeEach(ExtensionContext context) {
        this.releaseOutput();
        this.methodLevelCopy = new ByteArrayOutputStream();
        this.captureOutput(this.methodLevelCopy);
    }

    private void captureOutput(ByteArrayOutputStream copy) {
        OutputCapture.AnsiOutputControl.get().disableAnsiOutput();
        this.captureOut = new OutputCapture.CaptureOutputStream(System.out, copy);
        this.captureErr = new OutputCapture.CaptureOutputStream(System.err, copy);
        System.setOut(new PrintStream(this.captureOut));
        System.setErr(new PrintStream(this.captureErr));
    }

    private void releaseOutput() {
        if (this.captureOut != null) {
            OutputCapture.AnsiOutputControl.get().enabledAnsiOutput();
            System.setOut(this.captureOut.getOriginal());
            System.setErr(this.captureErr.getOriginal());
            this.methodLevelCopy = null;
        }
    }

    private void flush() {
        try {
            this.captureOut.flush();
            this.captureErr.flush();
        } catch (IOException var2) {
        }

    }

    public int length() {
        return this.toString().length();
    }

    public char charAt(int index) {
        return this.toString().charAt(index);
    }

    public CharSequence subSequence(int start, int end) {
        return this.toString().subSequence(start, end);
    }

    public String toString() {
        this.flush();
        if (this.classLevelCopy == null && this.methodLevelCopy == null) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();
            if (this.classLevelCopy != null) {
                builder.append(this.classLevelCopy.toString());
            }

            builder.append(this.methodLevelCopy.toString());
            return builder.toString();
        }
    }

    public void beforeAll(ExtensionContext context) {
        this.classLevelCopy = new ByteArrayOutputStream();
        this.captureOutput(this.classLevelCopy);
    }

    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return OutputCapture.class.equals(parameterContext.getParameter().getType());
    }

    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return this;
    }

    private static class AnsiPresentOutputControl extends OutputCapture.AnsiOutputControl {
        private AnsiPresentOutputControl() {
            super();
        }

        public void disableAnsiOutput() {
            AnsiOutput.setEnabled(Enabled.NEVER);
        }

        public void enabledAnsiOutput() {
            AnsiOutput.setEnabled(Enabled.DETECT);
        }
    }

    private static class AnsiOutputControl {
        private AnsiOutputControl() {
        }

        public void disableAnsiOutput() {
        }

        public void enabledAnsiOutput() {
        }

        public static OutputCapture.AnsiOutputControl get() {
            try {
                Class.forName("org.springframework.boot.ansi.AnsiOutput");
                return new OutputCapture.AnsiPresentOutputControl();
            } catch (ClassNotFoundException var1) {
                return new OutputCapture.AnsiOutputControl();
            }
        }
    }

    private static class CaptureOutputStream extends OutputStream {
        private final PrintStream original;
        private final OutputStream copy;

        CaptureOutputStream(PrintStream original, OutputStream copy) {
            this.original = original;
            this.copy = copy;
        }

        public void write(int b) throws IOException {
            this.copy.write(b);
            this.original.write(b);
            this.original.flush();
        }

        public void write(byte[] b) throws IOException {
            this.write(b, 0, b.length);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            this.copy.write(b, off, len);
            this.original.write(b, off, len);
        }

        public PrintStream getOriginal() {
            return this.original;
        }

        public void flush() throws IOException {
            this.copy.flush();
            this.original.flush();
        }
    }
}
