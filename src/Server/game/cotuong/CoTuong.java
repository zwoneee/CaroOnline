package Server.game.cotuong;

import Server.game.GameLogic;

public class CoTuong extends GameLogic {

    public CoTuong() {
        System.out.println("Co Tuong constructor");
    }

    @Override
    public String receiveDataFromClient(String received) {
        System.out.println("Game CoTuong received: " + received);
        return "";
    }
}
