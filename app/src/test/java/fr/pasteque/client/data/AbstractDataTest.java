package fr.pasteque.client.data;

import android.content.Context;
import fr.pasteque.client.Constant;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.models.Product;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;

import static org.easymock.EasyMock.*;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.powermock.api.easymock.PowerMock.mockStatic;

/**
 * Created by nsvir on 12/10/15.
 * n.svirchevsky@gmail.com
 */
@RunWith(PowerMockRunner.class)
//Used in setupd to mock the static method Pasteque.getAppContext
@PrepareForTest(Pasteque.class)
public abstract class AbstractDataTest {

    public static final String TMP_FILENAME_EMPTY = Constant.BUILD_FOLDER + "empty.data";

    protected Context fakeContext;

    public abstract String getTmpFilename();

    public String getFullTmpFilename() {
        return Constant.BUILD_FOLDER + getTmpFilename();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setup() throws IOException {
        this.fakeContext = createMock(Context.class);
        File file = new File(TMP_FILENAME_EMPTY);
        file.getParentFile().mkdirs();
        file.createNewFile();
        file = new File(getFullTmpFilename());
        file.getParentFile().mkdirs();
        file.createNewFile();
        mockStatic(Pasteque.class);
        expect(Pasteque.getAppContext()).andStubReturn(this.fakeContext);
    }

    protected void replayContext() {
        replay(this.fakeContext);
        PowerMock.replay(Pasteque.class);
    }

    protected IAnswer<FileInputStream> defaultInputIAnswer = new IAnswer<FileInputStream>() {
        @Override
        public FileInputStream answer() throws Throwable {
            return new FileInputStream(getFullTmpFilename());
        }
    };

    protected IAnswer<FileOutputStream> defaultOutputIAnswer = new IAnswer<FileOutputStream>() {
        @Override
        public FileOutputStream answer() throws Throwable {
            return new FileOutputStream(getFullTmpFilename());
        }
    };

    protected void addDefaultFileInputExpected() throws FileNotFoundException {
        addFileInputExpected(defaultInputIAnswer);
    }


    protected void addDefaultFileOutputExpected() throws FileNotFoundException {
        addFileOutputExpected(defaultOutputIAnswer);
    }


    protected void addFileInputExpected(IAnswer<FileInputStream> ianswer) throws FileNotFoundException {
        expect(fakeContext.openFileInput(anyObject(String.class))).andStubAnswer(ianswer);
    }

    protected void addFileOutputExpected(IAnswer<FileOutputStream> ianswer) throws FileNotFoundException {
        expect(fakeContext.openFileOutput(anyObject(String.class), anyInt())).andStubAnswer(ianswer);
    }
}
