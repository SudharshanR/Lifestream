package poke.Model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;




@Entity
public class ImageTable {
@Id

	String userId;

private byte[] image;



public String getUserId() {
	return userId;
}

public void setUserId(String userId) {
	this.userId = userId;
}

public byte[] getImage() {
	return image;
}

public void setImage(byte[] image) {
	this.image = image;
}
}
