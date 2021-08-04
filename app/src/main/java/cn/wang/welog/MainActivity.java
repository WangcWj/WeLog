package cn.wang.welog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.google.gson.Gson;
import com.wang.monitor.core.AppMonitor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import cn.wang.log.core.WeLog;
import cn.wang.welog.demo.ListNode;

public class MainActivity extends AppCompatActivity {

    View notice;

    private Handler mainHandler = new Handler();
    public static SparseArray<String> activityLifeCircle = new SparseArray();


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            WeLog.d("MainActivity run");
            notice.requestLayout();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        notice= findViewById(R.id.block);

        notice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, DialogActivity.class));
            }
        });

        mainHandler.postDelayed(runnable,1000);

        /*
         * 1.采用Builder方式来建造。
         * 2.日志的处理应该采用责任链的模式，可以仿照OkHttp。因为日志信息可能需要要经过很多的步骤，
         * 比如说日志格式化、系统打印日志、日志加密、日志写入文件等，这样的话还是采用责任链模式来说比较解耦，
         * 另外如果要替换某个步骤的实现也很简单。
         *
         *
         *
         *
         *
         *
         *
         *
         *
         *
         *
         *
         *
         * */
        int i = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (i == -1) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
            return;
        }
        //WeLog.dw("MainActivity");
        //NIO  184  54 45
        //IO  160 89 72


        ListNode listNode = new ListNode(0);
        ListNode listNode1 = new ListNode(1);
        ListNode listNode2 = new ListNode(2);
        ListNode listNode3 = new ListNode(3);
        ListNode listNode4 = new ListNode(4);
        ListNode listNode5 = new ListNode(5);
        ListNode listNode6 = new ListNode(6);
        ListNode listNode7 = new ListNode(7);
        ListNode listNode8 = new ListNode(8);


        listNode1.left = listNode2;
        listNode1.right = listNode3;

        listNode2.left = listNode4;
        listNode2.right = listNode5;

        listNode3.left = listNode6;
        listNode3.right = listNode7;

     /*   //广度遍历。

        List<ListNode> listNodes = new ArrayList<>();
        postorder(listNode1, listNodes);

        for (int j = 0; j < listNodes.size(); j++) {
            Log.e(" Wang", "MainActivity.onCreate." + listNodes.get(j).val);
        }*/
        dfs(listNode1);
     /*   ListNode centerListNode = bfs(listNode1);
        while (centerListNode != null) {
            Log.e("cc.wang", "MainActivity.onCreate." + centerListNode.val);
            centerListNode = centerListNode.next;
        }*/

    }

    private void postorder(ListNode listNode, List<ListNode> res) {
        trans(listNode, res);
    }

    private void trans(ListNode listNode, List<ListNode> res) {
        if (listNode == null) {
            return;
        }
        trans(listNode.left, res);
        trans(listNode.right, res);
        res.add(listNode);
    }


    private void postorderTraversal(ListNode listNode){



    }


    private void dfs(ListNode listNode1) {
        Deque<ListNode> deque = new ArrayDeque<>();
        deque.push(listNode1);
        while (deque.size() > 0) {
            ListNode poll = deque.pop();
            //1 2 4
            if (null == poll) {
                continue;
            }
            Log.e(" Wang", "MainActivity.dfs."+poll.val);
            if (poll.right != null) {
                deque.push(poll.right);
            }
            if(poll.left != null){
                deque.push(poll.left);
            }
        }


    }

    private ListNode bfs(ListNode listNode1) {
        ListNode all = new ListNode(-1);
        Queue<ListNode> queue = new LinkedList<>();
        queue.offer(listNode1);

        ListNode current = all;
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                ListNode poll = queue.poll();
                if (null == poll) {
                    continue;
                }
                current.next = poll;
                current = current.next;
                Log.e(" Wang", "MainActivity.bfs." + poll.val);
                if (poll.left != null) {
                    queue.offer(poll.left);
                }
                if (poll.right != null) {
                    queue.offer(poll.right);
                }
            }
        }

        return all.next;
    }


    public void sortListNode2(ListNode one, ListNode two) {
        if (one == null || two == null) {
            return;
        }
        ListNode A = one;
        ListNode B = two;
        while (A != B) {
            A = A == null ? B : A.next;
            B = B == null ? A : B.next;
        }
    }

    public ListNode sortListNode(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }
        ListNode fast = head.next;
        ListNode slow = head;
        while (fast != null && fast.next != null) {
            fast = fast.next.next;
            slow = slow.next;
        }
        ListNode temp = slow.next;
        slow.next = null;
        ListNode left = sortListNode(head);
        ListNode right = sortListNode(temp);

        ListNode result = new ListNode(-1);
        ListNode current = result;
        while (left != null && right != null) {
            if (left.val < right.val) {
                result.next = left;
                left = left.next;
            } else {
                result.next = right;
                right = right.next;
            }
            result = result.next;
        }
        result.next = left != null ? left : right;
        return current.next;
    }

    public ListNode getCenterListNode(ListNode listNode) {
        ListNode fast = listNode.next;
        ListNode slow = listNode;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }

    private boolean isValid(String s) {
        int length = s.length();
        if ((length % 2) == 1) {
            return false;
        }
        Map<Character, Character> res = new HashMap<>(5);
        res.put(')', '(');
        res.put('[', ']');
        res.put('{', '}');

        Deque<Character> stack = new ArrayDeque<>();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (res.containsKey(c)) {
                if (stack.isEmpty() || stack.peek() != res.get(c)) {
                    return false;
                }
                stack.pop();
            } else {
                stack.push(c);
            }
        }
        return stack.isEmpty();
    }

    /**
     * 合并两个有序列表。
     * <p>
     * 方法1 是循环拼接。就是采用分别遍历两个链表。
     * 方法2 就是递归。递归最要是确定终止的条件。
     *
     * @return
     */
    public ListNode mergeTwoList(ListNode one, ListNode two) {
        if (one == null) {
            return two;
        } else if (two == null) {
            return one;
        } else if (one.val > two.val) {
            two.next = mergeTwoList(one, two.next);
            return two;
        } else {
            one.next = mergeTwoList(one.next, two);
            return one;
        }
    }

    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        if (l1 == null) {
            return l2;
        } else if (l2 == null) {
            return l1;
        } else if (l1.val < l2.val) {
            l1.next = mergeTwoLists(l1.next, l2);
            return l1;
        } else {
            l2.next = mergeTwoLists(l1, l2.next);
            return l2;
        }

    }

    public ListNode mergeInBetween(ListNode list1, int a, int b, ListNode list2) {

        ListNode aNode = null;
        ListNode bNode = null;

        ListNode current = list1;

        int index = 0;
        while (current != null) {
            if (index < a) {
                aNode = current;
            }
            if (index == b + 1) {
                bNode = current;
                break;
            }
            index++;
            current = current.next;
        }
        aNode.next = null;
        ListNode tail = list2;
        while (tail != null && tail.next != null) {
            tail = tail.next;
        }
        aNode.next = list2;
        tail.next = bNode;
        return list1;
    }

    public ListNode reverseList(ListNode node) {
        if (node == null || node.next == null) {
            return node;
        }
        ListNode listNode = reverseList(node.next);
        node.next.next = node;
        node.next = null;
        return listNode;
    }

    public boolean isSameTree(ListNode p, ListNode q) {

        if (p == null && q == null) {
            return true;
        } else if (p == null || q == null) {
            return false;
        } else if (p.val == q.val) {
            return true;
        } else {
            return isSameTree(p.left, q.left) & isSameTree(p.right, q.right);
        }
    }


}