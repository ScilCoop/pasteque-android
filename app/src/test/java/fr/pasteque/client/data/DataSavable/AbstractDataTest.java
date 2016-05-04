package fr.pasteque.client.data.DataSavable;

import android.content.Context;
import fr.pasteque.client.Constant;
import fr.pasteque.client.Pasteque;
import fr.pasteque.client.models.Product;
import fr.pasteque.client.utils.file.*;
import fr.pasteque.client.utils.file.File;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import java.io.*;

import static org.easymock.EasyMock.*;
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
        java.io.File file = new java.io.File(TMP_FILENAME_EMPTY);
        file.getParentFile().mkdirs();
        file.createNewFile();
        file = new java.io.File(getFullTmpFilename());
        file.getParentFile().mkdirs();
        file.createNewFile();
        mockStatic(Pasteque.class);
        expect(Pasteque.getAppContext()).andStubReturn(this.fakeContext);
        expect(fakeContext.getDir(anyString(), anyInt())).andStubAnswer(new IAnswer<java.io.File>() {
            @Override
            public java.io.File answer() throws Throwable {
                return new java.io.File(Constant.BUILD_FOLDER);
            }
        });
    }

    protected void replayContext() {
        replay(this.fakeContext);
        PowerMock.replay(Pasteque.class);
    }

    protected File createDefaultTmpFile() {
        return new TestFile(getFullTmpFilename());
    }

    protected File createCustomFile(String s) {
        return new TestFile(s);
    }
}
