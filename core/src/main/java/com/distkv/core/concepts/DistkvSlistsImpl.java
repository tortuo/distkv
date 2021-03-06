package com.distkv.core.concepts;

import com.distkv.common.entity.sortedList.SlistEntity;
import com.distkv.common.exception.DistkvKeyDuplicatedException;
import com.distkv.common.DistkvTuple;
import com.distkv.common.exception.KeyNotFoundException;
import com.distkv.common.exception.SlistMemberNotFoundException;
import com.distkv.common.exception.SlistMembersDuplicatedException;
import com.distkv.common.exception.SlistIncrScoreOutOfRangeException;
import com.distkv.common.exception.SlistTopNumIsNonNegativeException;
import com.distkv.core.DistkvMapInterface;
import com.distkv.core.struct.slist.Slist;
import com.distkv.core.struct.slist.SlistLinkedImpl;

import java.util.List;
import java.util.LinkedList;

public class DistkvSlistsImpl
    extends DistkvConcepts<Slist>
    implements DistkvSlists {

  public DistkvSlistsImpl(DistkvMapInterface<String, Object> distkvKeyValueMap) {
    super(distkvKeyValueMap);
  }

  @Override
  public void put(String key, LinkedList<SlistEntity> list) {
    if (distkvKeyValueMap.containsKey(key)) {
      throw new DistkvKeyDuplicatedException(key);
    }
    Slist slist = new SlistLinkedImpl();
    if (!slist.put(list)) {
      throw new SlistMembersDuplicatedException(key);
    }
    distkvKeyValueMap.put(key, slist);
  }

  @Override
  public void putMember(String key, SlistEntity item) {
    if (!distkvKeyValueMap.containsKey(key)) {
      throw new KeyNotFoundException(key);
    }
    final Slist slist = get(key);
    slist.putItem(item);
  }

  @Override
  public void removeMember(String key, String member) {
    if (!distkvKeyValueMap.containsKey(key)) {
      throw new KeyNotFoundException(key);
    }
    final Slist slist = get(key);
    final boolean isFound = slist.removeItem(member);
    if (!isFound) {
      throw new SlistMemberNotFoundException(key);
    }
  }

  @Override
  public void incrScore(String key, String member, int delta) {
    if (!distkvKeyValueMap.containsKey(key)) {
      throw new KeyNotFoundException(key);
    }
    final Slist slist = get(key);
    final int resultByIncrScore = slist.incrScore(member, delta);
    if (0 == resultByIncrScore) {
      throw new SlistMemberNotFoundException(key);
    } else if (-1 == resultByIncrScore) {
      throw new SlistIncrScoreOutOfRangeException(key);
    }
  }

  @Override
  public List<SlistEntity> top(String key, int topNum) {
    if (!distkvKeyValueMap.containsKey(key)) {
      throw new KeyNotFoundException(key);
    }
    final Slist slist = get(key);
    if (topNum > slist.size()) {
      topNum = slist.size();
    }
    if (topNum <= 0) {
      throw new SlistTopNumIsNonNegativeException(key, topNum);
    }
    return slist.subList(1, topNum);
  }

  @Override
  public DistkvTuple<Integer, Integer> getMember(String key, String member) {
    if (!distkvKeyValueMap.containsKey(key)) {
      throw new KeyNotFoundException(key);
    }
    final Slist sortedLists = get(key);
    DistkvTuple<Integer, Integer> result = sortedLists.getItem(member);
    if (null == result) {
      throw new SlistMemberNotFoundException(key);
    }
    return result;
  }

}
