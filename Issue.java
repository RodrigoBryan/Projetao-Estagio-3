
package Aplicacao.model;

public interface Issue {

    public static enum IssueStatus {
        NEW, OPENED, FIXED, CLOSED
    }

    public String getId();
    public String getProjectName();
    public IssueStatus getStatus();
    public String getCod();
    public String getSynopsis();
    public String getDescription();
	public String getDescription2();
}
