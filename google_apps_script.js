function doPost(e) {
  try {
    var jsonString = e.postData.contents;
    var data = JSON.parse(jsonString);
    
    var base64Image = data.image;
    var fileName = data.name;
    var type = data.type; // "profile" atau "post"
    
    // ID Folder Google Drive dari proyek Anda
    var folderId = "";
    if (type === "profile") {
      folderId = "1HMelHUQx-MVSpY5QMmmlquiij03DkKym"; // Folder Foto Profil
    } else {
      folderId = "1YpJqPmlDg81qS1EpldeS8iD9JjgvqRqH"; // Folder Foto Postingan
    }
    
    var folder;
    try {
      folder = DriveApp.getFolderById(folderId);
    } catch (err) {
      // Fallback ke Root Drive jika folder ID tidak dapat diakses atau tidak ada izin edit
      folder = DriveApp.getRootFolder();
    }
    
    var decoded = Utilities.base64Decode(base64Image);
    var blob = Utilities.newBlob(decoded, "image/jpeg", fileName);
    
    var file = folder.createFile(blob);
    // Set permission agar foto bisa diakses oleh siapa saja yang memiliki link
    file.setSharing(DriveApp.Access.ANYONE_WITH_LINK, DriveApp.Permission.VIEW);
    
    var fileId = file.getId();
    // Format link langsung (direct link) agar pustaka Glide di Android bisa memuat gambar secara langsung
    var directUrl = "https://docs.google.com/uc?export=view&id=" + fileId;
    
    var response = {
      status: "success",
      url: directUrl
    };
    
    return ContentService.createTextOutput(JSON.stringify(response))
      .setMimeType(ContentService.MimeType.JSON);
      
  } catch (error) {
    var response = {
      status: "error",
      message: error.toString()
    };
    return ContentService.createTextOutput(JSON.stringify(response))
      .setMimeType(ContentService.MimeType.JSON);
  }
}
