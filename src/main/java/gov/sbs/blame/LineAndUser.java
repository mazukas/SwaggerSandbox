package gov.sbs.blame;

public class LineAndUser {
	private Integer line;
	private String user;
	
	public LineAndUser(Integer line, String user) {
		this.line = line;
		this.user = user;
	}
	
	public Integer getLine() {
		return line;
	}
	public void setLine(Integer line) {
		this.line = line;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
}
