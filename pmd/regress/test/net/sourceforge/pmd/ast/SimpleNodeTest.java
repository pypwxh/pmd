package test.net.sourceforge.pmd.ast;

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.ast.ASTBlock;
import net.sourceforge.pmd.ast.ASTBlockStatement;
import net.sourceforge.pmd.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.ast.ASTName;
import net.sourceforge.pmd.ast.ASTReturnStatement;
import net.sourceforge.pmd.ast.ASTUnmodifiedClassDeclaration;
import net.sourceforge.pmd.ast.SimpleNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SimpleNodeTest extends ParserTst {

    private static final String METHOD_DIFF_LINES =
    "public class Test {" + PMD.EOL +
    " public void foo() {" + PMD.EOL +
    "  int x;" + PMD.EOL +
    " }" + PMD.EOL +
    "}";

    public void testMethodDiffLines() throws Throwable {
        Set methods = getNodes(ASTMethodDeclaration.class, METHOD_DIFF_LINES);
        Iterator iter = methods.iterator();
        verifyNode((SimpleNode) iter.next(), 2, 2, 4, 2);
    }

    private static final String METHOD_SAME_LINE =
    "public class Test {" + PMD.EOL +
    " public void foo() {}" + PMD.EOL +
    "}";

    public void testMethodSameLine() throws Throwable {
        Set methods = getNodes(ASTMethodDeclaration.class, METHOD_SAME_LINE);
        Iterator iter = methods.iterator();
        verifyNode((SimpleNode) iter.next(), 2, 2, 2, 21);
    }


    public void testNoLookahead() throws Throwable {
        String code = "public class Foo { }"; // 1, 8 -> 1, 20
        Set uCD = getNodes(ASTUnmodifiedClassDeclaration.class, code);
        Iterator iter = uCD.iterator();
        verifyNode((SimpleNode) iter.next(), 1, 8, 1, 20);
    }

    private static final String QUALIFIED_NAME =
    "import java.io.File;" + PMD.EOL +
    "public class Foo{}";

    public void testNames() throws Throwable {
        Set name = getNodes(ASTName.class, QUALIFIED_NAME);
        Iterator i = name.iterator();
        while (i.hasNext()) {
            SimpleNode node = (SimpleNode) i.next();
            if (node.getImage().equals("java.io.File")) {
                verifyNode(node, 1, 8, 1, 19);
            }
        }
    }

    public void testNames2() throws Throwable {
        String code = "import java.io.\nFile; \n public class Foo{}";
        Set name = getNodes(ASTName.class, code);
        Iterator i = name.iterator();
        assertTrue(i.hasNext());

        while (i.hasNext()) {
            SimpleNode node = (SimpleNode) i.next();
            if (node.getImage().equals("java.io.File")) {
                verifyNode(node, 1, 8, 2, 4);
            }

            if (node.getImage().equals("Foo")) {
                verifyNode(node, 2, 15, 2, 18);
            }

        }
    }

    public void verifyNode(SimpleNode node, int beginLine, int beginCol, int endLine, int endCol) {
        assertEquals("Wrong beginning line: ", beginLine, node.getBeginLine());
        assertEquals("Wrong beginning column: ", beginCol, node.getBeginColumn());
        assertEquals("Wrong ending line:", endLine, node.getEndLine());
        assertEquals("Wrong ending column:", endCol, node.getEndColumn());
    }

    public void testFindChildrenOfType() {
        ASTBlock block = new ASTBlock(2);
        block.jjtAddChild(new ASTReturnStatement(1), 0);
        assertEquals(1, block.findChildrenOfType(ASTReturnStatement.class).size());
    }

    public void testFindChildrenOfTypeMultiple() {
        ASTBlock block = new ASTBlock(1);
        block.jjtAddChild(new ASTBlockStatement(2), 0);
        block.jjtAddChild(new ASTBlockStatement(3), 1);

        List nodes = new ArrayList();
        block.findChildrenOfType(ASTBlockStatement.class, nodes);
        assertEquals(2, nodes.size());
    }

    public void testFindChildrenOfTypeRecurse() {
        ASTBlock block = new ASTBlock(1);
        ASTBlock childBlock = new ASTBlock(2);
        block.jjtAddChild(childBlock, 0);
        childBlock.jjtAddChild(new ASTMethodDeclaration(3), 0);

        List nodes = new ArrayList();
        block.findChildrenOfType(ASTMethodDeclaration.class, nodes);
        assertEquals(1, nodes.size());
    }
}
