package com.noober.plugin.tiny;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.tinify.AccountException;
import com.tinify.Source;
import com.tinify.Tinify;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CompressAction extends AnAction {

    private String groupId = "111";
    private AnActionEvent anActionEvent;
    private JTextField jTextField;

    String hint = "Please enter the default key. If not, the default key will be used";

    @Override
    public void actionPerformed(AnActionEvent e) {
        anActionEvent = e;
        SampleDialogWrapper startDialog = new SampleDialogWrapper("start compress");
        startDialog.show();
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

    public class SampleDialogWrapper extends DialogWrapper {

        String msg;

        public SampleDialogWrapper(String msg) {
            super(true);
            this.msg = msg;
            init();
            getCancelAction().setEnabled(false);
            setTitle("TinyCompress");


        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            JPanel dialogPanel = new JPanel();
            jTextField = new JTextField(hint);
            dialogPanel.add(jTextField);
            return dialogPanel;
        }

        @Override
        protected void createDefaultActions() {
            super.createDefaultActions();
            myOKAction = new OkAction("OK");
        }

        protected class OkAction extends DialogWrapper.DialogWrapperAction {


            protected OkAction(@NotNull String name) {
                super(name);
                this.putValue("DefaultAction", Boolean.TRUE);
            }

            protected void doAction(ActionEvent e) {
                Notifications.Bus.notify(new Notification(groupId, "TinyCompress", "start compress", NotificationType.INFORMATION, null));
                String key;
                if(jTextField.getText().equals(hint)){
                    key = "LHZoJXCysEceDReZIsQPWPxdODBxhavW";
                }else {
                    key = jTextField.getText();
                }

                doOKAction();
                Observable.timer(3, TimeUnit.MILLISECONDS)
                        .subscribe(aLong -> {
                            Tinify.setKey(key);
                            ArrayList<String> imagePaths = new ArrayList<>();
                            for (VirtualFile file : getSelectFiles(anActionEvent)) {
                                imagePaths.addAll(getFileArrayList(file));
                            }
                            boolean result = true;
                            for (String path : imagePaths) {
                                Source source = null;
                                try {
                                    source = Tinify.fromFile(path);
                                    source.toFile(path);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                    if(e1 instanceof AccountException){
                                        result = false;
                                        Notifications.Bus.notify(new Notification(groupId, "TinyCompress", e1.getMessage(), NotificationType.WARNING, null));
                                        break;
                                    }else {
                                        Notifications.Bus.notify(new Notification(groupId, "TinyCompress", e1.getMessage(), NotificationType.WARNING, null), anActionEvent.getProject());
                                    }

                                }
                            }
                            if(result){
                                Notifications.Bus.notify(new Notification(groupId, "TinyCompress", "compress complete", NotificationType.INFORMATION, null));
                            }

                        });
            }
        }
    }
}