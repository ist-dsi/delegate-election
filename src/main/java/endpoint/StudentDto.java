package endpoint;

public class StudentDto {

	private String name;
	private String username;
	
	public StudentDto() {
	}

	public StudentDto(String name, String username, String email, String photoType, String photoBytes) {
    		this.name = name;
    		this.username = username;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && (obj instanceof StudentDto) && ((StudentDto) obj).getUsername().equals(username);
	}

	@Override
	public int hashCode() {
		return username.hashCode();
	}

}
