
public class Data {
public int ID;
public double value;

public Data(int ID, double value)
{
	this.ID = ID;
	this.value = value;
}

public int compareTo(Data o) {
  return Double.compare(value, o.value);
}
}