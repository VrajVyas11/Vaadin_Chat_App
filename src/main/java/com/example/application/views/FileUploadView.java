package com.example.application.views;

import com.example.application.services.FileService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.html.Anchor;

import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@PermitAll
@Route(value = "upload", layout = MainLayout.class)
@PageTitle("File Upload | Chat App")
public class FileUploadView extends VerticalLayout {

    private final FileService fileService;
    private Grid<String> fileGrid;
    private Paragraph emptyText;

    @Autowired
    public FileUploadView(FileService fileService) {
        this.fileService = fileService;

        // Apply dark theme
        getElement().getThemeList().add("dark");

        // Set page layout
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName("file-upload-view");

        // Set up main content container
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setMaxWidth("850px");
        contentContainer.setWidth("100%");
        contentContainer.setPadding(false);
        contentContainer.setSpacing(true);
        contentContainer.addClassName("content-container");
        contentContainer.setAlignItems(Alignment.CENTER);

        // Create elegant header with icon
        H2 title = new H2("File Management");
        title.addClassName("title-header");

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setSpacing(true);
        headerLayout.addClassName("header-layout");

        Icon fileIcon = new Icon(VaadinIcon.FOLDER_OPEN_O);
        fileIcon.addClassName("header-icon");

        headerLayout.add(fileIcon, title);

        // Upload section
        Div uploadSection = new Div();
        uploadSection.addClassName("section-card");
        uploadSection.setWidthFull();

        // Section header
        HorizontalLayout uploadHeaderLayout = new HorizontalLayout();
        uploadHeaderLayout.setWidthFull();
        uploadHeaderLayout.setAlignItems(Alignment.CENTER);
        uploadHeaderLayout.setSpacing(true);
        uploadHeaderLayout.addClassName("section-header");

        Icon uploadIcon = new Icon(VaadinIcon.UPLOAD);
        uploadIcon.addClassName("section-icon");

        Span uploadTitle = new Span("Upload Files");
        uploadTitle.addClassName("section-title");

        uploadHeaderLayout.add(uploadIcon, uploadTitle);

        // Upload component
        Div uploadContainer = createUploadComponent();
        uploadContainer.addClassName("upload-container");

        uploadSection.add(uploadHeaderLayout, uploadContainer);

        // Files section
        Div filesSection = new Div();
        filesSection.addClassName("section-card");
        filesSection.setWidthFull();

        // Section header
        HorizontalLayout filesHeaderLayout = new HorizontalLayout();
        filesHeaderLayout.setWidthFull();
        filesHeaderLayout.setAlignItems(Alignment.CENTER);
        filesHeaderLayout.setSpacing(true);
        filesHeaderLayout.addClassName("section-header");

        Icon filesIcon = new Icon(VaadinIcon.DATABASE);
        filesIcon.addClassName("section-icon");

        Span filesTitle = new Span("Uploaded Files");
        filesTitle.addClassName("section-title");

        filesHeaderLayout.add(filesIcon, filesTitle);

        // File listing component
        fileGrid = new Grid<>();
        fileGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        fileGrid.setWidthFull();
        fileGrid.setHeight("300px");
        fileGrid.addClassName("file-grid");

        // Configure grid
        fileGrid.addColumn(fileName -> fileName)
            .setHeader("File Name")
            .setFlexGrow(1)
            .setAutoWidth(true);

        fileGrid.addComponentColumn(fileName -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);
            actions.setJustifyContentMode(JustifyContentMode.END);
            actions.setWidthFull();
            actions.addClassName("file-actions");

            // Download button
            Button downloadButton = new Button(new Icon(VaadinIcon.DOWNLOAD_ALT));
            downloadButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            downloadButton.addClassName("action-button");
            downloadButton.getElement().setAttribute("aria-label", "Download file");
            downloadButton.getElement().setAttribute("title", "Download file");

            // Create download anchor and attach to button
            Anchor downloadLink = createDownloadLink(fileName);
            downloadLink.getElement().setAttribute("download", true);
            downloadLink.add(downloadButton);

            // Delete button
            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.addClassName("action-button");
            deleteButton.getElement().setAttribute("aria-label", "Delete file");
            deleteButton.getElement().setAttribute("title", "Delete file");

            deleteButton.addClickListener(e -> {
                boolean deleted = fileService.deleteFile(fileName);
                if (deleted) {
                    Notification.show("File deleted successfully",
                            3000, Notification.Position.TOP_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    refreshFileList();
                } else {
                    Notification.show("Failed to delete file",
                            3000, Notification.Position.TOP_END)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            });

            actions.add(downloadLink, deleteButton);
            return actions;
        }).setHeader("Actions").setTextAlign(ColumnTextAlign.END).setWidth("160px").setFlexGrow(0);

        // Empty state message
        emptyText = new Paragraph("No files uploaded yet");
        emptyText.addClassName("empty-message");

        // Add components to sections
        filesSection.add(filesHeaderLayout, fileGrid, emptyText);

        // Add all components to content container
        contentContainer.add(headerLayout, uploadSection, filesSection);

        // Add content container to view
        add(contentContainer);
        setHorizontalComponentAlignment(Alignment.CENTER, contentContainer);
        refreshFileList();
    }

    private Div createUploadComponent() {
        Div uploadContainer = new Div();
        uploadContainer.setWidthFull();
        uploadContainer.setClassName("upload-container");

        MemoryBuffer memoryBuffer = new MemoryBuffer();
        Upload upload = new Upload(memoryBuffer);
        upload.setWidthFull();
        upload.setMaxFiles(1);

        Paragraph dropLabel = new Paragraph("Drag files here or click to browse");

        // HorizontalLayout dropLabelLayout = new HorizontalLayout();
        // // dropLabelLayout.setAlignItems(Alignment.CENTER);
        // // dropLabelLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        // dropLabelLayout.add(dropLabel);

        upload.setDropLabel(dropLabel);
        upload.setAcceptedFileTypes("image/*", ".pdf", ".txt", ".doc", ".docx");
        upload.setMaxFileSize(10 * 1024 * 1024);
        upload.addClassName("upload-component");

        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            try {
                fileService.saveFile(fileName, memoryBuffer.getInputStream());
                Notification.show("File uploaded successfully", 
                        3000, Notification.Position.TOP_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshFileList();
            } catch (IOException e) {
                Notification.show("Failed to save file: " + e.getMessage(),
                        5000, Notification.Position.TOP_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        upload.addFailedListener(event -> {
            Notification.show("Upload failed: " + event.getReason(),
                    5000, Notification.Position.TOP_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        uploadContainer.add(upload);
        return uploadContainer;
    }

    private void refreshFileList() {
        List<String> files = fileService.getAllFiles();
        fileGrid.setItems(files);
        emptyText.setVisible(files.isEmpty()); // Show or hide the empty message
    }

    private Anchor createDownloadLink(String fileName) {
        Path filePath = fileService.getFilePath(fileName);
        StreamResource resource = new StreamResource(fileName, () -> {
            try {
                return new ByteArrayInputStream(Files.readAllBytes(filePath));
            } catch (IOException e) {
                e.printStackTrace();
                return new ByteArrayInputStream(new byte[0]);
            }
        });

        Anchor downloadLink = new Anchor(resource, "");
        downloadLink.getElement().setAttribute("download", true);
        downloadLink.addClassName("download-link");

        return downloadLink;
    }
}
