package com.noober.plugin.tiny;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.messages.MessageDialog;
import com.intellij.openapi.vfs.VirtualFile;
import com.tinify.Source;
import com.tinify.Tinify;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;

public class CompressAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        Observable.create((ObservableOnSubscribe<Boolean>) observableEmitter -> {
            Tinify.setKey("LHZoJXCysEceDReZIsQPWPxdODBxhavW");
            try {
                ArrayList<String> imagePaths = new ArrayList<>();
                for (VirtualFile file : getSelectFiles(e)) {
                    imagePaths.addAll(getFileArrayList(file));
                }
                for (String path : imagePaths) {
                    Source source = Tinify.fromFile(path);
                    source.toFile(path);
                }
                System.out.print("压缩完成");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            observableEmitter.onNext(true);
        }).subscribeOn(Schedulers.io()).subscribe();
//                .observeOn(Schedulers.trampoline()).subscribe(aBoolean ->
//                Messages.showMessageDialog("压缩完成", "TinyCompress", Messages.getInformationIcon()));

    }

    private VirtualFile[] getSelectFiles(AnActionEvent e) {
        return DataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
    }


    private ArrayList<String> getFileArrayList(VirtualFile file) {
        ArrayList<String> pathList = new ArrayList<>();
        if (!file.isDirectory()) {
            if (file.getPath().endsWith(".jpg") || file.getPath().endsWith(".jpeg") || file.getPath().endsWith(".png")) {
                pathList.add(file.getPath());
            }
        } else {
            for (VirtualFile file1 : file.getChildren()) {
                pathList.addAll(getFileArrayList(file1));
            }
        }
        return pathList;
    }
}