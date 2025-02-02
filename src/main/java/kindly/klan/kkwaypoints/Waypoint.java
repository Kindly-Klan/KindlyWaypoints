package kindly.klan.kkwaypoints;

public class Waypoint {
    private String name;
    private int x, y, z;
    private String world;
    private String texture;
    private boolean visible;

    public Waypoint(String name, int x, int y, int z, String world, String texture, boolean visible) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.texture = texture;
        this.visible = visible;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getZ() { return z; }
    public void setZ(int z) { this.z = z; }

    public String getWorld() { return world; }
    public void setWorld(String world) { this.world = world; }

    public String getTexture() { return texture; }
    public void setTexture(String texture) { this.texture = texture; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
}