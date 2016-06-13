
package Aplicacao.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class TrackingServiceStub implements TrackingService {

	String url = "jdbc:sqlserver://localhost:1433;databaseName=Academico;user=sa;password=1234;";

    // You add a project by adding an entry with an empty observable array list
    // of issue IDs in the projects Map.
    final ObservableMap<String, ObservableList<String>> projectsMap;
    {
        final Map<String, ObservableList<String>> map = new TreeMap<String, ObservableList<String>>();
        projectsMap = FXCollections.observableMap(map);
        for (String s : newList("Alunos", "Disciplinas", "Matriculas")) {
            projectsMap.put(s, FXCollections.<String>observableArrayList());
        }
    }

    // The projectNames list is kept in sync with the project's map by observing
    // the projectsMap and modifying the projectNames list in consequence.
    final MapChangeListener<String, ObservableList<String>> projectsMapChangeListener = new MapChangeListener<String, ObservableList<String>>() {
        @Override
        public void onChanged(Change<? extends String, ? extends ObservableList<String>> change) {
            if (change.wasAdded()) projectNames.add(change.getKey());
            if (change.wasRemoved()) projectNames.remove(change.getKey());
        }
    };
    final ObservableList<String> projectNames;
    {
        projectNames = FXCollections.<String>observableArrayList();
        projectNames.addAll(projectsMap.keySet());
        projectsMap.addListener(projectsMapChangeListener);
    }

    // A Issue stub.
    public final class IssueStub implements ObservableIssue {
        private final SimpleStringProperty id;
        private final SimpleStringProperty cod;
        private final SimpleStringProperty projectName;
        private final SimpleStringProperty title;
        private final SimpleStringProperty description;
        private final SimpleStringProperty description2;
        private final SimpleObjectProperty<IssueStatus> status =
                new SimpleObjectProperty<IssueStatus>(IssueStatus.NEW);

        IssueStub(String projectName, String id) {
            this(projectName, id, null);
        }
        IssueStub(String projectName, String id, String title) {
            assert projectNames.contains(projectName);
            assert ! projectsMap.get(projectName).contains(id);
            assert ! issuesMap.containsKey(id);
            this.projectName = new SimpleStringProperty(projectName);
            this.id = new SimpleStringProperty(id);
            this.cod = new SimpleStringProperty("null");
            this.title = new SimpleStringProperty(title);
            this.description = new SimpleStringProperty("");
            this.description2 = new SimpleStringProperty("");
        }

        @Override
        public IssueStatus getStatus() {
            return status.get();
        }

        @Override
        public String getId() {
            return id.get();
        }

        @Override
        public String getCod() {
            return cod.get();
        }

        @Override
        public String getProjectName() {
            return projectName.get();
        }

        @Override
        public String getSynopsis() {
            return title.get();
        }

        private void setSynopsis(String title) {
            this.title.set(title);
        }

        @Override
        public String getDescription() {
            return description.get();
        }

        @Override
        public String getDescription2() {
            return description2.get();
        }

        private void setDescription(String description) {
            this.description.set(description);
        }

        private void setDescription2(String description2) {
            this.description2.set(description2);
        }

        private void setCod(String cod) {
            this.cod.set(cod);
        }

        /*private void setStatus(IssueStatus issueStatus) {
            this.status.set(issueStatus);
        }*/

        @Override
        public ObservableValue<String> idProperty() {
            return id;
        }

        @Override
        public ObservableValue<String> projectNameProperty() {
            return projectName;
        }

        @Override
        public ObservableValue<IssueStatus> statusProperty() {
            return status;
        }

        @Override
        public ObservableValue<String> synopsisProperty() {
            return title;
        }

        @Override
        public ObservableValue<String> descriptionProperty() {
            return description;
        }

    }

    // You create new issue by adding a IssueStub instance to the issuesMap.
    // the new id will be automatically added to the corresponding list in
    // the projectsMap.
    //
    final MapChangeListener<String, IssueStub> issuesMapChangeListener = new MapChangeListener<String, IssueStub>() {
        @Override
        public void onChanged(Change<? extends String, ? extends IssueStub> change) {
            if (change.wasAdded()) {
                final IssueStub val = change.getValueAdded();
                projectsMap.get(val.getProjectName()).add(val.getId());
            }
            if (change.wasRemoved()) {
                final IssueStub val = change.getValueRemoved();
                projectsMap.get(val.getProjectName()).remove(val.getId());
            }
        }
    };

    final AtomicInteger issueCounter = new AtomicInteger(0);
    final ObservableMap<String, IssueStub> issuesMap;
    {
        final Map<String, IssueStub> map = new TreeMap<String, IssueStub>();
        issuesMap = FXCollections.observableMap(map);
        issuesMap.addListener(issuesMapChangeListener);
        IssueStub ts;
        try{
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			Connection con = DriverManager.getConnection(url);
			Statement st = con.createStatement();
			Statement st2 = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM Alunos");
			ResultSet rs2;
			while(rs.next()){
				ts = createIssueFor("Alunos");
				String cod = rs.getString("Cod");
				ts.setCod(cod);
				String nome = rs.getString("Nome");
				ts.setSynopsis(nome);
				String sexo = rs.getString("Sexo");
				ts.setDescription(sexo);
				rs2 = st2.executeQuery("select D.nome as Disciplina from matriculas as M inner join Alunos as A on (M.CodAluno = A.Cod) inner join Disciplinas as D on (M.CodDisc = D.Cod) where A.cod = "+cod);
				while(rs2.next()){
					ts.setDescription2(ts.getDescription2()+rs2.getString("Disciplina")+"\n");
				}
			}
			rs = st.executeQuery("SELECT * FROM Disciplinas");
			while(rs.next()){
				ts = createIssueFor("Disciplinas");
				String cod = rs.getString("Cod");
				ts.setCod(cod);
				String nome = rs.getString("Nome");
				ts.setSynopsis(nome);
				rs2 = st2.executeQuery("select A.nome as Aluno from matriculas as M inner join Alunos as A on (M.CodAluno = A.Cod) inner join Disciplinas as D on (M.CodDisc = D.Cod) where D.cod = "+cod);
				while(rs2.next()){
					ts.setDescription2(ts.getDescription2()+rs2.getString("Aluno")+"\n");
				}
			}

			rs = st.executeQuery("SELECT * FROM Matriculas");
			while(rs.next()){
				ts = createIssueFor("Matriculas");
				String cod = rs.getString("Codaluno");
				ts.setSynopsis(cod);
				String nome = rs.getString("Coddisc");
				ts.setDescription(nome);

			}

			rs.close();
			st.close();
			con.close();
		}catch(ClassNotFoundException e){

			e.printStackTrace();
		} catch (SQLException e) {

			e.printStackTrace();
		}
    }

    @SafeVarargs
	private static <T> List<T> newList(T... items) {
        return Arrays.asList(items);
    }


    @Override
    public IssueStub createIssueFor(String projectName) {
        assert projectNames.contains(projectName);
        final IssueStub issue = new IssueStub(projectName, "TT-"+issueCounter.incrementAndGet());
        assert issuesMap.containsKey(issue.getId()) == false;
        assert projectsMap.get(projectName).contains(issue.getId()) == false;
        issuesMap.put(issue.getId(), issue);
        if(projectName!= "Matriculas"){
	        try{
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
				Connection con = DriverManager.getConnection(url);
				Statement st = con.createStatement();
				ResultSet rs = st.executeQuery("SELECT * FROM " + projectName + " where cod in (select max(cod) from "+ projectName+")");
				rs.next();
				System.out.println(rs.getString("Cod"));
				String cod = rs.getString("Cod");
				issue.setCod(cod);
				String nome = rs.getString("Nome");
				issue.setSynopsis(nome);
				st.close();
				con.close();
			}catch(ClassNotFoundException e){

				e.printStackTrace();
			} catch (SQLException e) {

				e.printStackTrace();
			}
        }

        return issue;
    }

    @Override

    public void deleteIssue(String issueId) {
    	IssueStub issue = getIssue(issueId);
    	String projectName = issue.getProjectName();
        if(projectName == "Matriculas"){
	        AtualizaTabelas(issue);
        }
        assert issuesMap.containsKey(issueId);
        issuesMap.remove(issueId);

    }

    @Override
    public ObservableList<String> getProjectNames() {
        return projectNames;
    }

    @Override
    public ObservableList<String> getIssueIds(String projectName) {
        return projectsMap.get(projectName);
    }

    @Override
    public IssueStub getIssue(String issueId) {
        return issuesMap.get(issueId);
    }

    @Override
    public String getIssueId(String project, String issueCod) {
    	ObservableList<String> displayedIssues;
    	displayedIssues = projectsMap.get(project);
    	String retorno = "none";
    	int cod1 = Integer.parseInt(issueCod);
    	for (String id : displayedIssues) {
            final ObservableIssue issue = getIssue(id);
            String cod = issue.getCod();
            int cod2 = Integer.parseInt(cod);
            if(cod1 == cod2){
            	retorno = issue.getId();
            }

        }
    	return retorno;
    }


    public void AtualizaTabelas(IssueStub issue){
    	try{
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			Connection con = DriverManager.getConnection(url);
			Statement st = con.createStatement();
			Statement st2 = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM alunos where cod = "+issue.getSynopsis());
			ResultSet rs2;
			while(rs.next()){
				String cod = rs.getString("Cod");
				String Id = getIssueId("Alunos", cod);
				if(Id != "none"){
					IssueStub aluno = getIssue(Id);
					aluno.setDescription2("");
					rs2 = st2.executeQuery("select D.nome as Disciplina from matriculas as M inner join Alunos as A on (M.CodAluno = A.Cod) inner join Disciplinas as D on (M.CodDisc = D.Cod) where A.cod = "+cod);
					while(rs2.next()){
						aluno.setDescription2(aluno.getDescription2()+rs2.getString("Disciplina")+"\n");
					}
				}
			}
			rs = st.executeQuery("SELECT * FROM disciplinas where cod = "+issue.getDescription());
			while(rs.next()){
				String cod = rs.getString("Cod");
				String Id = getIssueId("Disciplinas", cod);
				if(Id != "none"){
					IssueStub disciplina = getIssue(Id);
					disciplina.setDescription2("");
					rs2 = st2.executeQuery("select A.nome as Aluno from matriculas as M inner join Alunos as A on (M.CodAluno = A.Cod) inner join Disciplinas as D on (M.CodDisc = D.Cod) where D.cod = "+cod);
					while(rs2.next()){
						disciplina.setDescription2(disciplina.getDescription2()+rs2.getString("Aluno")+"\n");
					}
				}
			}
			st2.close();
			rs.close();
			st.close();
			con.close();
		}catch(ClassNotFoundException e){

			e.printStackTrace();
		} catch (SQLException e) {

			e.printStackTrace();
		}
    }


    @Override
    public void saveIssue(String issueId, String synopsis, String description) {
        IssueStub issue = getIssue(issueId);
        issue.setDescription(description);
        issue.setSynopsis(synopsis);
        String projectName = issue.getProjectName();
        if(projectName == "Matriculas"){
	        AtualizaTabelas(issue);
        }

    }

}
