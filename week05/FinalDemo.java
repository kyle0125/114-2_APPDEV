package week05;

public class FinalDemo {

    
    static final int MAX_DEPTH = 11034;  

    
    static class Ship {
        protected String name;

        public Ship(String name) {
            this.name = name;
        }

        
        public final String type() {
            return "船舶";
        }

        
        public String sail() {
            return name + " 正在航行";
        }
    }

    
    static class FishingBoat extends Ship {

        public FishingBoat(String name) {
            super(name);
        }

        
        @Override
        public String sail() {
            return name + " 正在拖網捕魚";
        }

        
    }

    public static void main(String[] args) {
        System.out.println("馬里亞納海溝最深：" + MAX_DEPTH + " 公尺");

        

        Ship s = new Ship("遠洋號");
        FishingBoat f = new FishingBoat("海豐號");

        System.out.println(s.type() + "：" + s.sail());
        System.out.println(f.type() + "：" + f.sail());

        
        Ship s2 = new FishingBoat("福星號");
        System.out.println(s2.type() + "：" + s2.sail());
    }
}