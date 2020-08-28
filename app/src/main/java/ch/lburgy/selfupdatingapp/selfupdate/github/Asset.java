package ch.lburgy.selfupdatingapp.selfupdate.github;

public class Asset {
    private String url;
    private String browser_download_url;
    private float id;
    private String node_id;
    private String name;
    private String label;
    private String state;
    private String content_type;
    private float size;
    private float download_count;
    private String created_at;
    private String updated_at;
    Uploader UploaderObject;


    // Getter Methods

    public String getUrl() {
        return url;
    }

    public String getBrowser_download_url() {
        return browser_download_url;
    }

    public float getId() {
        return id;
    }

    public String getNode_id() {
        return node_id;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getState() {
        return state;
    }

    public String getContent_type() {
        return content_type;
    }

    public float getSize() {
        return size;
    }

    public float getDownload_count() {
        return download_count;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public Uploader getUploader() {
        return UploaderObject;
    }

    // Setter Methods

    public void setUrl(String url) {
        this.url = url;
    }

    public void setBrowser_download_url(String browser_download_url) {
        this.browser_download_url = browser_download_url;
    }

    public void setId(float id) {
        this.id = id;
    }

    public void setNode_id(String node_id) {
        this.node_id = node_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public void setDownload_count(float download_count) {
        this.download_count = download_count;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public void setUploader(Uploader uploaderObject) {
        this.UploaderObject = uploaderObject;
    }
}
