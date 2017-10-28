begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|querymatcher
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NavigableSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|Cell
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|CellComparator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|CellUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|PrivateCellUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|KeyValue
operator|.
name|Type
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|querymatcher
operator|.
name|ScanQueryMatcher
operator|.
name|MatchCode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * A tracker both implementing ColumnTracker and DeleteTracker, used for mvcc-sensitive scanning.  * We should make sure in one QueryMatcher the ColumnTracker and DeleteTracker is the same instance.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|NewVersionBehaviorTracker
implements|implements
name|ColumnTracker
implements|,
name|DeleteTracker
block|{
specifier|private
name|byte
index|[]
name|lastCqArray
decl_stmt|;
specifier|private
name|int
name|lastCqLength
decl_stmt|;
specifier|private
name|int
name|lastCqOffset
decl_stmt|;
specifier|private
name|long
name|lastCqTs
decl_stmt|;
specifier|private
name|long
name|lastCqMvcc
decl_stmt|;
specifier|private
name|byte
name|lastCqType
decl_stmt|;
specifier|private
name|int
name|columnIndex
decl_stmt|;
specifier|private
name|int
name|countCurrentCol
decl_stmt|;
specifier|protected
name|int
name|maxVersions
decl_stmt|;
specifier|private
name|int
name|resultMaxVersions
decl_stmt|;
specifier|private
name|byte
index|[]
index|[]
name|columns
decl_stmt|;
specifier|private
name|int
name|minVersions
decl_stmt|;
specifier|private
name|long
name|oldestStamp
decl_stmt|;
specifier|private
name|CellComparator
name|comparator
decl_stmt|;
comment|// These two maps have same structure.
comment|// Each node is a versions deletion (DeleteFamily or DeleteColumn). Key is the mvcc of the marker,
comment|// value is a data structure which contains infos we need that happens before this node's mvcc and
comment|// after the previous node's mvcc. The last node is a special node whose key is max_long that
comment|// saves infos after last deletion. See DeleteVersionsNode's comments for details.
comment|// The delColMap is constructed and used for each cq, and thedelFamMap is constructed when cq is
comment|// null and saving family-level delete markers. Each time the cq is changed, we should
comment|// reconstruct delColMap as a deep copy of delFamMap.
specifier|protected
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|DeleteVersionsNode
argument_list|>
name|delColMap
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|protected
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|DeleteVersionsNode
argument_list|>
name|delFamMap
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**    * Note maxVersion and minVersion must set according to cf's conf, not user's scan parameter.    *    * @param columns           columns specified user in query    * @param comparartor       the cell comparator    * @param minVersion        The minimum number of versions to keep(used when TTL is set).    * @param maxVersion        The maximum number of versions in CF's conf    * @param resultMaxVersions maximum versions to return per column, which may be different from    *                          maxVersion    * @param oldestUnexpiredTS the oldest timestamp we are interested in, based on TTL    */
specifier|public
name|NewVersionBehaviorTracker
parameter_list|(
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
parameter_list|,
name|CellComparator
name|comparartor
parameter_list|,
name|int
name|minVersion
parameter_list|,
name|int
name|maxVersion
parameter_list|,
name|int
name|resultMaxVersions
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|)
block|{
name|this
operator|.
name|maxVersions
operator|=
name|maxVersion
expr_stmt|;
name|this
operator|.
name|minVersions
operator|=
name|minVersion
expr_stmt|;
name|this
operator|.
name|resultMaxVersions
operator|=
name|resultMaxVersions
expr_stmt|;
name|this
operator|.
name|oldestStamp
operator|=
name|oldestUnexpiredTS
expr_stmt|;
if|if
condition|(
name|columns
operator|!=
literal|null
operator|&&
name|columns
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|columns
operator|=
operator|new
name|byte
index|[
name|columns
operator|.
name|size
argument_list|()
index|]
index|[]
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|column
range|:
name|columns
control|)
block|{
name|this
operator|.
name|columns
index|[
name|i
operator|++
index|]
operator|=
name|column
expr_stmt|;
block|}
block|}
name|this
operator|.
name|comparator
operator|=
name|comparartor
expr_stmt|;
name|reset
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|beforeShipped
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Do nothing
block|}
comment|/**    * A data structure which contains infos we need that happens before this node's mvcc and    * after the previous node's mvcc. A node means there is a version deletion at the mvcc and ts.    */
specifier|protected
class|class
name|DeleteVersionsNode
block|{
specifier|public
name|long
name|ts
decl_stmt|;
specifier|public
name|long
name|mvcc
decl_stmt|;
comment|//<timestamp, set<mvcc>>
comment|// Key is ts of version deletes, value is its mvccs.
comment|// We may delete more than one time for a version.
specifier|private
name|Map
argument_list|<
name|Long
argument_list|,
name|SortedSet
argument_list|<
name|Long
argument_list|>
argument_list|>
name|deletesMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|//<mvcc, set<mvcc>>
comment|// Key is mvcc of version deletes, value is mvcc of visible puts before the delete effect.
specifier|private
name|NavigableMap
argument_list|<
name|Long
argument_list|,
name|SortedSet
argument_list|<
name|Long
argument_list|>
argument_list|>
name|mvccCountingMap
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|protected
name|DeleteVersionsNode
parameter_list|(
name|long
name|ts
parameter_list|,
name|long
name|mvcc
parameter_list|)
block|{
name|this
operator|.
name|ts
operator|=
name|ts
expr_stmt|;
name|this
operator|.
name|mvcc
operator|=
name|mvcc
expr_stmt|;
name|mvccCountingMap
operator|.
name|put
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
operator|new
name|TreeSet
argument_list|<
name|Long
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|DeleteVersionsNode
parameter_list|()
block|{
name|this
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|addVersionDelete
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|SortedSet
argument_list|<
name|Long
argument_list|>
name|set
init|=
name|deletesMap
operator|.
name|get
argument_list|(
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|set
operator|==
literal|null
condition|)
block|{
name|set
operator|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
expr_stmt|;
name|deletesMap
operator|.
name|put
argument_list|(
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|set
argument_list|)
expr_stmt|;
block|}
name|set
operator|.
name|add
argument_list|(
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
comment|// The init set should be the puts whose mvcc is smaller than this Delete. Because
comment|// there may be some Puts masked by them. The Puts whose mvcc is larger than this Delete can
comment|// not be copied to this node because we may delete one version and the oldest put may not be
comment|// masked.
name|SortedSet
argument_list|<
name|Long
argument_list|>
name|nextValue
init|=
name|mvccCountingMap
operator|.
name|ceilingEntry
argument_list|(
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|SortedSet
argument_list|<
name|Long
argument_list|>
name|thisValue
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|nextValue
operator|.
name|headSet
argument_list|(
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|mvccCountingMap
operator|.
name|put
argument_list|(
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|,
name|thisValue
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|DeleteVersionsNode
name|getDeepCopy
parameter_list|()
block|{
name|DeleteVersionsNode
name|node
init|=
operator|new
name|DeleteVersionsNode
argument_list|(
name|ts
argument_list|,
name|mvcc
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|SortedSet
argument_list|<
name|Long
argument_list|>
argument_list|>
name|e
range|:
name|deletesMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|node
operator|.
name|deletesMap
operator|.
name|put
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|SortedSet
argument_list|<
name|Long
argument_list|>
argument_list|>
name|e
range|:
name|mvccCountingMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|node
operator|.
name|mvccCountingMap
operator|.
name|put
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|node
return|;
block|}
block|}
comment|/**    * Reset the map if it is different with the last Cell.    * Save the cq array/offset/length for next Cell.    *    * @return If this put has duplicate ts with last cell, return the mvcc of last cell.    * Else return MAX_VALUE.    */
specifier|protected
name|long
name|prepare
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|boolean
name|matchCq
init|=
name|PrivateCellUtil
operator|.
name|matchingQualifier
argument_list|(
name|cell
argument_list|,
name|lastCqArray
argument_list|,
name|lastCqOffset
argument_list|,
name|lastCqLength
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|matchCq
condition|)
block|{
comment|// The last cell is family-level delete and this is not, or the cq is changed,
comment|// we should construct delColMap as a deep copy of delFamMap.
name|delColMap
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|DeleteVersionsNode
argument_list|>
name|e
range|:
name|delFamMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|delColMap
operator|.
name|put
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getDeepCopy
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|countCurrentCol
operator|=
literal|0
expr_stmt|;
block|}
if|if
condition|(
name|matchCq
operator|&&
operator|!
name|PrivateCellUtil
operator|.
name|isDelete
argument_list|(
name|lastCqType
argument_list|)
operator|&&
name|lastCqType
operator|==
name|cell
operator|.
name|getTypeByte
argument_list|()
operator|&&
name|lastCqTs
operator|==
name|cell
operator|.
name|getTimestamp
argument_list|()
condition|)
block|{
comment|// Put with duplicate timestamp, ignore.
return|return
name|lastCqMvcc
return|;
block|}
name|lastCqArray
operator|=
name|cell
operator|.
name|getQualifierArray
argument_list|()
expr_stmt|;
name|lastCqOffset
operator|=
name|cell
operator|.
name|getQualifierOffset
argument_list|()
expr_stmt|;
name|lastCqLength
operator|=
name|cell
operator|.
name|getQualifierLength
argument_list|()
expr_stmt|;
name|lastCqTs
operator|=
name|cell
operator|.
name|getTimestamp
argument_list|()
expr_stmt|;
name|lastCqMvcc
operator|=
name|cell
operator|.
name|getSequenceId
argument_list|()
expr_stmt|;
name|lastCqType
operator|=
name|cell
operator|.
name|getTypeByte
argument_list|()
expr_stmt|;
return|return
name|Long
operator|.
name|MAX_VALUE
return|;
block|}
comment|// DeleteTracker
annotation|@
name|Override
specifier|public
name|void
name|add
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|prepare
argument_list|(
name|cell
argument_list|)
expr_stmt|;
name|byte
name|type
init|=
name|cell
operator|.
name|getTypeByte
argument_list|()
decl_stmt|;
switch|switch
condition|(
name|Type
operator|.
name|codeToType
argument_list|(
name|type
argument_list|)
condition|)
block|{
comment|// By the order of seen. We put null cq at first.
case|case
name|DeleteFamily
case|:
comment|// Delete all versions of all columns of the specified family
name|delFamMap
operator|.
name|put
argument_list|(
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|,
operator|new
name|DeleteVersionsNode
argument_list|(
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|DeleteFamilyVersion
case|:
comment|// Delete all columns of the specified family and specified version
name|delFamMap
operator|.
name|ceilingEntry
argument_list|(
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|)
operator|.
name|getValue
argument_list|()
operator|.
name|addVersionDelete
argument_list|(
name|cell
argument_list|)
expr_stmt|;
break|break;
comment|// These two kinds of markers are mix with Puts.
case|case
name|DeleteColumn
case|:
comment|// Delete all versions of the specified column
name|delColMap
operator|.
name|put
argument_list|(
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|,
operator|new
name|DeleteVersionsNode
argument_list|(
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|Delete
case|:
comment|// Delete the specified version of the specified column.
name|delColMap
operator|.
name|ceilingEntry
argument_list|(
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|)
operator|.
name|getValue
argument_list|()
operator|.
name|addVersionDelete
argument_list|(
name|cell
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Unknown delete marker type for "
operator|+
name|cell
argument_list|)
throw|;
block|}
block|}
comment|/**    * This method is not idempotent, we will save some info to judge VERSION_MASKED.    * @param cell - current cell to check if deleted by a previously seen delete    * @return We don't distinguish DeleteColumn and DeleteFamily. We only return code for column.    */
annotation|@
name|Override
specifier|public
name|DeleteResult
name|isDeleted
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|long
name|duplicateMvcc
init|=
name|prepare
argument_list|(
name|cell
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|DeleteVersionsNode
argument_list|>
name|e
range|:
name|delColMap
operator|.
name|tailMap
argument_list|(
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|)
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|DeleteVersionsNode
name|node
init|=
name|e
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|long
name|deleteMvcc
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
name|SortedSet
argument_list|<
name|Long
argument_list|>
name|deleteVersionMvccs
init|=
name|node
operator|.
name|deletesMap
operator|.
name|get
argument_list|(
name|cell
operator|.
name|getTimestamp
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|deleteVersionMvccs
operator|!=
literal|null
condition|)
block|{
name|SortedSet
argument_list|<
name|Long
argument_list|>
name|tail
init|=
name|deleteVersionMvccs
operator|.
name|tailSet
argument_list|(
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|tail
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|deleteMvcc
operator|=
name|tail
operator|.
name|first
argument_list|()
expr_stmt|;
block|}
block|}
name|SortedMap
argument_list|<
name|Long
argument_list|,
name|SortedSet
argument_list|<
name|Long
argument_list|>
argument_list|>
name|subMap
init|=
name|node
operator|.
name|mvccCountingMap
operator|.
name|subMap
argument_list|(
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|,
literal|true
argument_list|,
name|Math
operator|.
name|min
argument_list|(
name|duplicateMvcc
argument_list|,
name|deleteMvcc
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Long
argument_list|,
name|SortedSet
argument_list|<
name|Long
argument_list|>
argument_list|>
name|seg
range|:
name|subMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|seg
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
operator|>=
name|maxVersions
condition|)
block|{
return|return
name|DeleteResult
operator|.
name|VERSION_MASKED
return|;
block|}
name|seg
operator|.
name|getValue
argument_list|()
operator|.
name|add
argument_list|(
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|deleteMvcc
operator|<
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
return|return
name|DeleteResult
operator|.
name|VERSION_DELETED
return|;
block|}
if|if
condition|(
name|cell
operator|.
name|getTimestamp
argument_list|()
operator|<=
name|node
operator|.
name|ts
condition|)
block|{
return|return
name|DeleteResult
operator|.
name|COLUMN_DELETED
return|;
block|}
block|}
if|if
condition|(
name|duplicateMvcc
operator|<
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
return|return
name|DeleteResult
operator|.
name|VERSION_MASKED
return|;
block|}
return|return
name|DeleteResult
operator|.
name|NOT_DELETED
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|delColMap
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|delColMap
operator|.
name|get
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|mvccCountingMap
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|delFamMap
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|delFamMap
operator|.
name|get
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|mvccCountingMap
operator|.
name|size
argument_list|()
operator|==
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|update
parameter_list|()
block|{
comment|// ignore
block|}
comment|//ColumnTracker
annotation|@
name|Override
specifier|public
name|MatchCode
name|checkColumn
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|byte
name|type
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|done
argument_list|()
condition|)
block|{
comment|// No more columns left, we are done with this query
return|return
name|ScanQueryMatcher
operator|.
name|MatchCode
operator|.
name|SEEK_NEXT_ROW
return|;
comment|// done_row
block|}
if|if
condition|(
name|columns
operator|!=
literal|null
condition|)
block|{
while|while
condition|(
name|columnIndex
operator|<
name|columns
operator|.
name|length
condition|)
block|{
name|int
name|c
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|columns
index|[
name|columnIndex
index|]
argument_list|,
literal|0
argument_list|,
name|columns
index|[
name|columnIndex
index|]
operator|.
name|length
argument_list|,
name|cell
operator|.
name|getQualifierArray
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierOffset
argument_list|()
argument_list|,
name|cell
operator|.
name|getQualifierLength
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|<
literal|0
condition|)
block|{
name|columnIndex
operator|++
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|c
operator|==
literal|0
condition|)
block|{
comment|// We drop old version in #isDeleted, so here we must return INCLUDE.
return|return
name|MatchCode
operator|.
name|INCLUDE
return|;
block|}
else|else
block|{
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_COL
return|;
block|}
block|}
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_ROW
return|;
block|}
return|return
name|MatchCode
operator|.
name|INCLUDE
return|;
block|}
annotation|@
name|Override
specifier|public
name|MatchCode
name|checkVersions
parameter_list|(
name|Cell
name|cell
parameter_list|,
name|long
name|timestamp
parameter_list|,
name|byte
name|type
parameter_list|,
name|boolean
name|ignoreCount
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
operator|!
name|PrivateCellUtil
operator|.
name|isDelete
argument_list|(
name|type
argument_list|)
assert|;
comment|// We drop old version in #isDeleted, so here we won't SKIP because of versioning. But we should
comment|// consider TTL.
if|if
condition|(
name|ignoreCount
condition|)
block|{
return|return
name|MatchCode
operator|.
name|INCLUDE
return|;
block|}
name|countCurrentCol
operator|++
expr_stmt|;
if|if
condition|(
name|timestamp
operator|<
name|this
operator|.
name|oldestStamp
condition|)
block|{
if|if
condition|(
name|countCurrentCol
operator|==
name|minVersions
condition|)
block|{
return|return
name|MatchCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_COL
return|;
block|}
if|if
condition|(
name|countCurrentCol
operator|>
name|minVersions
condition|)
block|{
comment|// This may not be reached, only for safety.
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_COL
return|;
block|}
block|}
if|if
condition|(
name|countCurrentCol
operator|==
name|resultMaxVersions
condition|)
block|{
comment|// We have enough number of versions for user's requirement.
return|return
name|MatchCode
operator|.
name|INCLUDE_AND_SEEK_NEXT_COL
return|;
block|}
if|if
condition|(
name|countCurrentCol
operator|>
name|resultMaxVersions
condition|)
block|{
comment|// This may not be reached, only for safety
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_COL
return|;
block|}
return|return
name|MatchCode
operator|.
name|INCLUDE
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|delColMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|delFamMap
operator|.
name|clear
argument_list|()
expr_stmt|;
name|lastCqArray
operator|=
literal|null
expr_stmt|;
name|lastCqLength
operator|=
literal|0
expr_stmt|;
name|lastCqOffset
operator|=
literal|0
expr_stmt|;
name|lastCqTs
operator|=
name|Long
operator|.
name|MIN_VALUE
expr_stmt|;
name|lastCqMvcc
operator|=
literal|0
expr_stmt|;
name|lastCqType
operator|=
literal|0
expr_stmt|;
name|columnIndex
operator|=
literal|0
expr_stmt|;
name|countCurrentCol
operator|=
literal|0
expr_stmt|;
name|resetInternal
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|resetInternal
parameter_list|()
block|{
name|delFamMap
operator|.
name|put
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|,
operator|new
name|DeleteVersionsNode
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|done
parameter_list|()
block|{
comment|// lastCq* have been updated to this cell.
return|return
operator|!
operator|(
name|columns
operator|==
literal|null
operator|||
name|lastCqArray
operator|==
literal|null
operator|)
operator|&&
name|Bytes
operator|.
name|compareTo
argument_list|(
name|lastCqArray
argument_list|,
name|lastCqOffset
argument_list|,
name|lastCqLength
argument_list|,
name|columns
index|[
name|columnIndex
index|]
argument_list|,
literal|0
argument_list|,
name|columns
index|[
name|columnIndex
index|]
operator|.
name|length
argument_list|)
operator|>
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|ColumnCount
name|getColumnHint
parameter_list|()
block|{
if|if
condition|(
name|columns
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|columnIndex
operator|<
name|columns
operator|.
name|length
condition|)
block|{
return|return
operator|new
name|ColumnCount
argument_list|(
name|columns
index|[
name|columnIndex
index|]
argument_list|)
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|MatchCode
name|getNextRowOrNextColumn
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
comment|// TODO maybe we can optimize.
return|return
name|MatchCode
operator|.
name|SEEK_NEXT_COL
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isDone
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
comment|// We can not skip Cells with small ts.
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|CellComparator
name|getCellComparator
parameter_list|()
block|{
return|return
name|this
operator|.
name|comparator
return|;
block|}
block|}
end_class

end_unit

