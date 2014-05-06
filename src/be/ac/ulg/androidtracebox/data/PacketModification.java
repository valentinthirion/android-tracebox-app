package be.ac.ulg.androidtracebox.data;

public class PacketModification {
	private String layer;
	private String field;
	private int id;

	public PacketModification(String m)
	{
		// IP::Modif or TCP::Modif
		String[] splited = m.split("::");
		setLayer(splited[0]);
		setField(splited[1]);
	}

	public String getLayer() {
		return layer;
	}

	public void setLayer(String layer) {
		this.layer = layer;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
