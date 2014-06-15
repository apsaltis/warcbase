package org.warcbase.analysis.graph;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.warcbase.data.UriMapping;

import au.com.bytecode.opencsv.CSVReader;

public class PrefixMapping {
  public class PrefixNode {
    int id;
    String url;
    int startPos;
    int endPos;

    public PrefixNode(int id, String url, int startPos, int endPos) {
      this.id = id;
      this.url = url;
      this.startPos = startPos;
      this.endPos = endPos;
    }

    public int getId() {
      return id;
    }

    public String getUrl() {
      return url;
    }

    public int getStartPos() {
      return startPos;
    }

    public int getEndPos() {
      return endPos;
    }
  }

  public static List<PrefixNode> loadPrefix(String prefixFile, UriMapping map)
      throws IOException {
    PrefixMapping instance = new PrefixMapping();
    final Comparator<PrefixNode> comparator = new Comparator<PrefixNode>() {
      @Override
      public int compare(PrefixNode n1, PrefixNode n2) {
        if (n1.startPos > n2.startPos) {
          return 1;
        } else if (n1.startPos == n2.startPos) {
          return 0;
        } else {
          return -1;
        }
      }
    };
    List<PrefixNode> prefixes = new ArrayList<PrefixNode>();
    CSVReader reader = new CSVReader(new FileReader(prefixFile), ',');
    reader.readNext();
    String[] record = null;
    while ((record = reader.readNext()) != null) {
      int id = Integer.valueOf(record[0]);
      String url = record[1];
      List<String> results = map.prefixSearch(url);
      int[] boundary = map.getIdRange(results.get(0), results.get(results.size() - 1));
      PrefixNode node = instance.new PrefixNode(id, url, boundary[0], boundary[1]);
      prefixes.add(node);
    }
    Collections.sort(prefixes, comparator);
    reader.close();
    return prefixes;
  }

  public int getPrefixId(int id, List<PrefixNode> prefixes) {
    int start = 0, end = prefixes.size() - 1;
    int mid;
    while (start <= end) {
      mid = (start + end) / 2;
      if (prefixes.get(mid).getStartPos() <= id && prefixes.get(mid).getEndPos() >= id) {
        return prefixes.get(mid).getId();
      } else if (prefixes.get(mid).getStartPos() > id) {
        end = mid - 1;
      } else {
        start = mid + 1;
      }
    }
    return -1;
  }
}
