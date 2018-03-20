begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
operator|.
name|visibility
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
name|ArrayList
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
name|List
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
name|KeyValue
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
name|Tag
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
name|NewVersionBehaviorTracker
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Similar to MvccSensitiveTracker but tracks the visibility expression also before  * deciding if a Cell can be considered deleted  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|VisibilityNewVersionBehaivorTracker
extends|extends
name|NewVersionBehaviorTracker
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|VisibilityNewVersionBehaivorTracker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|VisibilityNewVersionBehaivorTracker
parameter_list|(
name|NavigableSet
argument_list|<
name|byte
index|[]
argument_list|>
name|columns
parameter_list|,
name|CellComparator
name|cellComparator
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
name|super
argument_list|(
name|columns
argument_list|,
name|cellComparator
argument_list|,
name|minVersion
argument_list|,
name|maxVersion
argument_list|,
name|resultMaxVersions
argument_list|,
name|oldestUnexpiredTS
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|TagInfo
block|{
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
decl_stmt|;
name|Byte
name|format
decl_stmt|;
specifier|private
name|TagInfo
parameter_list|(
name|Cell
name|c
parameter_list|)
block|{
name|tags
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|format
operator|=
name|VisibilityUtils
operator|.
name|extractVisibilityTags
argument_list|(
name|c
argument_list|,
name|tags
argument_list|)
expr_stmt|;
block|}
specifier|private
name|TagInfo
parameter_list|()
block|{
name|tags
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
class|class
name|VisibilityDeleteVersionsNode
extends|extends
name|DeleteVersionsNode
block|{
specifier|private
name|TagInfo
name|tagInfo
decl_stmt|;
comment|//<timestamp, set<mvcc>>
comment|// Key is ts of version deletes, value is its mvccs.
comment|// We may delete more than one time for a version.
specifier|private
name|Map
argument_list|<
name|Long
argument_list|,
name|SortedMap
argument_list|<
name|Long
argument_list|,
name|TagInfo
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
name|VisibilityDeleteVersionsNode
parameter_list|(
name|long
name|ts
parameter_list|,
name|long
name|mvcc
parameter_list|,
name|TagInfo
name|tagInfo
parameter_list|)
block|{
name|this
operator|.
name|tagInfo
operator|=
name|tagInfo
expr_stmt|;
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
annotation|@
name|Override
specifier|protected
name|VisibilityDeleteVersionsNode
name|getDeepCopy
parameter_list|()
block|{
name|VisibilityDeleteVersionsNode
name|node
init|=
operator|new
name|VisibilityDeleteVersionsNode
argument_list|(
name|ts
argument_list|,
name|mvcc
argument_list|,
name|tagInfo
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
name|SortedMap
argument_list|<
name|Long
argument_list|,
name|TagInfo
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
name|TreeMap
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
annotation|@
name|Override
specifier|public
name|void
name|addVersionDelete
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|SortedMap
argument_list|<
name|Long
argument_list|,
name|TagInfo
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
name|TreeMap
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
name|put
argument_list|(
name|cell
operator|.
name|getSequenceId
argument_list|()
argument_list|,
operator|new
name|TagInfo
argument_list|(
name|cell
argument_list|)
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
block|}
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
name|KeyValue
operator|.
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
name|VisibilityDeleteVersionsNode
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
argument_list|,
operator|new
name|TagInfo
argument_list|(
name|cell
argument_list|)
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
name|VisibilityDeleteVersionsNode
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
argument_list|,
operator|new
name|TagInfo
argument_list|(
name|cell
argument_list|)
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
specifier|private
name|boolean
name|tagMatched
parameter_list|(
name|Cell
name|put
parameter_list|,
name|TagInfo
name|delInfo
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|Tag
argument_list|>
name|putVisTags
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Byte
name|putCellVisTagsFormat
init|=
name|VisibilityUtils
operator|.
name|extractVisibilityTags
argument_list|(
name|put
argument_list|,
name|putVisTags
argument_list|)
decl_stmt|;
return|return
name|putVisTags
operator|.
name|isEmpty
argument_list|()
operator|==
name|delInfo
operator|.
name|tags
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|(
operator|(
name|putVisTags
operator|.
name|isEmpty
argument_list|()
operator|&&
name|delInfo
operator|.
name|tags
operator|.
name|isEmpty
argument_list|()
operator|)
operator|||
name|VisibilityLabelServiceManager
operator|.
name|getInstance
argument_list|()
operator|.
name|getVisibilityLabelService
argument_list|()
operator|.
name|matchVisibility
argument_list|(
name|putVisTags
argument_list|,
name|putCellVisTagsFormat
argument_list|,
name|delInfo
operator|.
name|tags
argument_list|,
name|delInfo
operator|.
name|format
argument_list|)
operator|)
return|;
block|}
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
try|try
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
name|VisibilityDeleteVersionsNode
name|node
init|=
operator|(
name|VisibilityDeleteVersionsNode
operator|)
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
name|SortedMap
argument_list|<
name|Long
argument_list|,
name|TagInfo
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
name|SortedMap
argument_list|<
name|Long
argument_list|,
name|TagInfo
argument_list|>
name|tail
init|=
name|deleteVersionMvccs
operator|.
name|tailMap
argument_list|(
name|cell
operator|.
name|getSequenceId
argument_list|()
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
name|TagInfo
argument_list|>
name|entry
range|:
name|tail
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|tagMatched
argument_list|(
name|cell
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
condition|)
block|{
name|deleteMvcc
operator|=
name|tail
operator|.
name|firstKey
argument_list|()
expr_stmt|;
break|break;
block|}
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
operator|&&
name|tagMatched
argument_list|(
name|cell
argument_list|,
name|node
operator|.
name|tagInfo
argument_list|)
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
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Error in isDeleted() check! Will treat cell as not deleted"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
return|return
name|DeleteResult
operator|.
name|NOT_DELETED
return|;
block|}
annotation|@
name|Override
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
name|VisibilityDeleteVersionsNode
argument_list|(
name|Long
operator|.
name|MIN_VALUE
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|,
operator|new
name|TagInfo
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

