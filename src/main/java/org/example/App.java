package org.example;

import com.toomasr.sgf4j.Sgf;
import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.GameNode;
import com.toomasr.sgf4j.parser.Util;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final DecimalFormat df = new DecimalFormat("0.0");

    private static final ScriptEngineManager mgr = new ScriptEngineManager();
    private static final ScriptEngine engine = mgr.getEngineByName("JavaScript");
    public static void main( String[] args )
    {
        //readFileWithParser();
        //testParseAISenseiComment();

        /*List<ScriptEngineFactory> factories = mgr.getEngineFactories();
        for (ScriptEngineFactory factory : factories)
            System.out.println(factory.getEngineName() + " " + factory.getEngineVersion() + " " + factory.getNames());
        if (factories.isEmpty())
            System.out.println("No Script Engines found");
        Expression expression = new ExpressionBuilder("3+2").build();
        double result = expression.evaluate();
        System.out.println(" result! "+result); //5*/
        testReadOGSFile();


        //testParseOGSComment();
    }

    private static void testReadOGSFile() {

        //String filePath = "C:\\Users\\yamak\\Documents\\jeff_jouni_3k.sgf";
        //String filePath = "C:\\Users\\yamak\\Documents\\jeff_jouni_3k - Copy.sgf";
        String filePath = "C:\\Users\\yamak\\Downloads\\43472267-167-\uD83D\uDC2CSofiam\uD83D\uDC2C-nutpen (1).sgf";
        Game game  =  getSGFFromPath(filePath);
        GameNode node = game.getRootNode();
        boolean isWhiteWin = true;
        Float whiteRank = -30.f;
        Float blackRank = -30.f;

        System.out.println("Properties");
        System.out.println(game.getProperties());

        /*String foo = "40+2";
        System.out.println(engine.eval(foo));*/

        whiteRank = rankTransformer(game.getProperty("WR").replace("k","*-1").replace("d","").replace("p","+7"));
        blackRank = rankTransformer(game.getProperty("BR"));
        String endResult = game.getProperty("RE");
        if(endResult.startsWith("B+")) {
            isWhiteWin= false;
        } else if (endResult.startsWith("W+")) {
            isWhiteWin = true;
        } else {
            throw new RuntimeException("unable to parse end result");
        }

        int cpt = 0;
        int cptMax = 68;
        do {
            //System.out.println("------------------------------------------------");
            //    System.out.println(node);
            //System.out.println("Move "+node.getColor()+node.getMoveNo()+": "+(node.getMoveNo()>0?(node.getCoords()[0] +" - "+node.getCoords()[1]):"?-?") +" child: "+node.getChildren().size() +" -> "+node.getSgfComment().replace("\n"," AND "));
            boolean isWhite = "W".equals(node.getColor());
            if(node.getMoveNo() >0) {
                if(node.getChildren().isEmpty()) {
                    if(node.getNextNode() != null) {
                        throw new RuntimeException("no analysis for the end of the game (move "+node.getMoveNo()+")");
                    }
                    System.out.println(node.getMoveNo() + ";" + node.getColor() + ";" + (isWhite == isWhiteWin ? "100.0" : "0.0") + ";" + (isWhite ? whiteRank : blackRank));
                } else {
                    System.out.println(node.getMoveNo() + ";" + node.getColor() + ";" + df.format(minimaxOGS(node, isWhite)[0]) + ";" + (isWhite ? whiteRank : blackRank));
                }
            }

            if ( false && (
                    cpt ==63 ||
                    cpt ==64 ||
                    cpt ==65 ||
                    cpt ==66)
                    && !node.getChildren().isEmpty()) {
                System.out.println("------------------------------------------------"+node.getChildren().contains(node.getNextNode()));
                //    System.out.println(node);
                System.out.println("Move "+node.getColor()+node.getMoveNo()+": "+(node.getMoveNo()>0?(node.getCoords()[0] +" - "+node.getCoords()[1]):"?-?") +" child: "+node.getChildren().size() +" -> "+node.getSgfComment().replace("\n"," AND "));

                //System.out.println("Minimax Back WR ["+minimaxOGS(node, isWhite)[0]+"-"+minimaxOGS(node, isWhite)[1]+"] ----> WR ="+("B".equals(node.getColor()) ? minimaxOGS(node)[1] : minimaxOGS(node)[0]));
                System.out.println("Minimax Back WR ["+minimaxOGS(node, isWhite)[0]+"-"+minimaxOGS(node, isWhite)[1]+"] ----> WR ="+(minimaxOGS(node, isWhite)[0]));

                /*Iterator<GameNode> it= node.getChildren().iterator();
                GameNode onechild = it.next();
                System.out.println(onechild.getId()+" Move children are "+onechild.getColor()+onechild.getMoveNo()+": "+onechild.getMoveString() +" child: "+onechild.getChildren().size() +" ("+readMoveCommentFromOGS(onechild.getSgfComment()).get("Black WR")+") -> "+onechild.getSgfComment().replace("\n"," AND "));
                System.out.println(onechild.getId()+" Move "+ onechild.getProperties().get("AW").toString());
                int []moveCoords = Util.alphaToCoords(onechild.getProperties().get("AW"));
                System.out.println(onechild.getId()+" Move "+ moveCoords[0]+" - "+moveCoords[1]);
                onechild = it.next();
                System.out.println(onechild.getId()+" Move children are "+onechild.getColor()+onechild.getMoveNo()+": "+onechild.getMoveString() +" child: "+onechild.getChildren().size() +" ("+readMoveCommentFromOGS(onechild.getSgfComment()).get("Black WR")+") -> "+onechild.getSgfComment().replace("\n"," AND "));
*/
            }

            cpt++;
        }
        while ((node = node.getNextNode()) != null /*&& cpt < cptMax*/);
/*
        System.out.println("-----c: "+node.getChildren().size()+ " ------c: "+node.getChildren()+ "---------------");
        //System.out.println(node.getCoords());
        System.out.println(node.getMoveNo());
        System.out.println(node.getMoveString());
        System.out.println(node.getSgfComment());
        node= node.getNextNode();

        System.out.println("-----c: "+node.getChildren().size()+ " ------c: "+node.getChildren()+ "---------------");
        System.out.println("Move "+node.getMoveNo()+": "+(node.getMoveNo()>0?(node.getCoords()[0] +" - "+node.getCoords()[1]):"?-?") +" -> "+node.getSgfComment().replace("\n"," AND "));
*/
    }

    private static Float rankTransformer(String rankAsString) {
            Expression expression = new ExpressionBuilder(
                    rankAsString
                            .replace("k","*-1")
                            .replace("d","")
                            .replace("p","+7")).build();
            return new Float(expression.evaluate());
    }

    /**
     *
     * @param node
     * @param isWhite true if we interpret White WR
     * @return [min WR, max WR] among children
     */
    public static Float[] minimaxOGS(GameNode node, boolean isWhite){
        Float [] result = {101.f,-1.f};
        for (GameNode oneChild: node.getChildren()) {
            Float OGSCommentWR = (Float)readMoveCommentFromOGS(oneChild.getSgfComment()).get("Black WR");
            Float winRate = isWhite ? 100-OGSCommentWR : OGSCommentWR;

            if(winRate.compareTo(result[0]) <0) {
                result[0] = winRate;
            }
            if(winRate.compareTo(result[0]) >0) {
                result[1] = winRate;
            }
        }
        return result;
    }

    private static void readFileWithParser() {

        //String filePath = "C:\\Users\\yamak\\Documents\\jeff_jouni_3k.sgf";
        //String filePath = "C:\\Users\\yamak\\Documents\\jeff_jouni_3k - Copy.sgf";
        String filePath = "C:\\Users\\yamak\\Downloads\\ai-sensei_20000921_Ishida-Yoshio_vs_Cho-U.sgf";
        Game game  =  getSGFFromPath(filePath);
        GameNode node = game.getRootNode();
        do {
            System.out.println("------------------------------------------------");
            //    System.out.println(node);
            System.out.println("Move "+node.getColor()+node.getMoveNo()+": "+(node.getMoveNo()>0?(node.getCoords()[0] +" - "+node.getCoords()[1]):"?-?") +" -> "+node.getSgfComment().replace("\n"," AND "));

        }
        while ((node = node.getNextNode()) != null);
/*
        System.out.println("-----c: "+node.getChildren().size()+ " ------c: "+node.getChildren()+ "---------------");
        //System.out.println(node.getCoords());
        System.out.println(node.getMoveNo());
        System.out.println(node.getMoveString());
        System.out.println(node.getSgfComment());
        node= node.getNextNode();

        System.out.println("-----c: "+node.getChildren().size()+ " ------c: "+node.getChildren()+ "---------------");
        System.out.println("Move "+node.getMoveNo()+": "+(node.getMoveNo()>0?(node.getCoords()[0] +" - "+node.getCoords()[1]):"?-?") +" -> "+node.getSgfComment().replace("\n"," AND "));
*/
    }

    private static Game getSGFFromPath(String path) {
        try {
            String gameAsString = new String(Files.readAllBytes(Paths.get(path)), "UTF-8");
            gameAsString = gameAsString.replace("KTV[1.0]","");
            return Sgf.createFromString(gameAsString);
        } catch (IOException var3) {
            throw new RuntimeException(var3);
        }
    }

    private static void testParseAISenseiComment() {

    String comment1 = "This move is 0.1 points worse than the AI move. AND  AND White is ahead by 0.3 points.";
    System.out.println("------------------------------------------------");
    System.out.println(comment1);
    System.out.println(readMoveCommentFromAISensei(comment1, "B"));
    comment1 = "White is ahead by 0.3 points.";
    System.out.println("------------------------------------------------");
    System.out.println(comment1);
    System.out.println(readMoveCommentFromAISensei(comment1, "B"));
    comment1 = "This move is 0.1 points worse than the AI move. AND  AND White is ahead by 0.4 points.";
    System.out.println("------------------------------------------------");
    System.out.println(comment1);
    System.out.println(readMoveCommentFromAISensei(comment1, "B"));
    comment1 = "White is ahead by 0.3 points.";
    System.out.println("------------------------------------------------");
    System.out.println(comment1);
    System.out.println(readMoveCommentFromAISensei(comment1, "B"));
    comment1 = "This move is 0.3 points worse than the AI move. AND  AND White is ahead by 0.6 points.";
    System.out.println("------------------------------------------------");
    System.out.println(comment1);
    System.out.println(readMoveCommentFromAISensei(comment1, "B"));
    comment1 = "Black is ahead by 0.3 points.";
    System.out.println("------------------------------------------------");
    System.out.println(comment1);
    System.out.println(readMoveCommentFromAISensei(comment1, "B"));
    comment1 = "This move is 0.3 points worse than the AI move. AND  AND The game is even.";
    System.out.println("------------------------------------------------");
    System.out.println(comment1);
    System.out.println(readMoveCommentFromAISensei(comment1, "B"));
    comment1 = "This move is 0.6 points worse than the AI move. AND  AND Black is ahead by 0.6 points.";
    System.out.println("------------------------------------------------");
    System.out.println(comment1);
    System.out.println(readMoveCommentFromAISensei(comment1, "B"));
}
    private static void testParseOGSComment() {

    String comment1 = "Black's win rate: %60.2 visits: 1";
    System.out.println("------------------------------------------------");
    System.out.println(comment1);
    System.out.println(readMoveCommentFromOGS(comment1));

}

    private static Map<String, Object> readMoveCommentFromAISensei(String aiSenseiComment, String currentMoveColor) {
        Map<String, Object> result = new HashMap<>();
        // get current move color

        // current score in points
        String scoreMarker = " is ahead by ";
        String evenMarker = "The game is even.";
        if(aiSenseiComment.indexOf(scoreMarker)>0) {
            String [] stringsAroundMarker = aiSenseiComment.split(scoreMarker);
            String [] wordsBeforeMarker = stringsAroundMarker[stringsAroundMarker.length-1].split(" ");
            String [] wordsAfterMarker = stringsAroundMarker[stringsAroundMarker.length-1].split(" ");
            Float mistakeAmount = Float.parseFloat(wordsAfterMarker[0]);
            String whoLeads = wordsBeforeMarker[wordsBeforeMarker.length-1].substring(0,1);
            result.put("score", whoLeads.equals(currentMoveColor) ? mistakeAmount : -mistakeAmount);
        } else if(aiSenseiComment.indexOf(evenMarker)>=0) {
            result.put("score", 0);
        }


        // mistake in points
        String mistakeMarker = " points worse than the AI move.";
        if(aiSenseiComment.indexOf(mistakeMarker)>0) {
            String [] wordsBeforeMarker = aiSenseiComment.split(mistakeMarker)[0].split(" ");
            Float mistakeAmount = Float.parseFloat(wordsBeforeMarker[wordsBeforeMarker.length-1]);
            result.put("mistake", mistakeAmount);
        }

        return result;
    }

    /**
     * Black's win rate: %74.6 visits: 1
     * @param aiSenseiComment
     * @return
     */
    private static Map<String, Object> readMoveCommentFromOGS(String aiSenseiComment) {
        Map<String, Object> result = new HashMap<>();
        // get current move color

        // current score in points
        String scoreMarker = "Black's win rate: %";
        //System.out.println(scoreMarker+ " -> "+aiSenseiComment.indexOf(scoreMarker));
        if(aiSenseiComment.indexOf(scoreMarker)>=0) {
            String [] stringsAroundMarker = aiSenseiComment.split(scoreMarker);
            String [] wordsAfterMarker = stringsAroundMarker[stringsAroundMarker.length-1].split(" ");
            Float blackWR = Float.parseFloat(wordsAfterMarker[0]);
            if(blackWR<0) {
                blackWR = 0.f;
            } else if (blackWR > 100) {
                blackWR = 100.0f;
            }
            result.put("Black WR", Math.round(10*blackWR)/10.f);
        }

        return result;
    }

}
