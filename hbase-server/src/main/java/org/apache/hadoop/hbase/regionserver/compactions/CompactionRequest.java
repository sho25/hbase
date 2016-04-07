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
name|regionserver
operator|.
name|compactions
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Function
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Joiner
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Predicate
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Collections2
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
name|Collection
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|classification
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
name|classification
operator|.
name|InterfaceStability
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
name|Store
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
name|StoreFile
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
name|StoreFile
operator|.
name|Reader
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
name|EnvironmentEdgeManager
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * This class holds all logical details necessary to run a compaction.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
block|{
literal|"coprocessor"
block|}
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|CompactionRequest
implements|implements
name|Comparable
argument_list|<
name|CompactionRequest
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|CompactionRequest
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// was this compaction promoted to an off-peak
specifier|private
name|boolean
name|isOffPeak
init|=
literal|false
decl_stmt|;
specifier|private
enum|enum
name|DisplayCompactionType
block|{
name|MINOR
block|,
name|ALL_FILES
block|,
name|MAJOR
block|}
specifier|private
name|DisplayCompactionType
name|isMajor
init|=
name|DisplayCompactionType
operator|.
name|MINOR
decl_stmt|;
specifier|private
name|int
name|priority
init|=
name|Store
operator|.
name|NO_PRIORITY
decl_stmt|;
specifier|private
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|filesToCompact
decl_stmt|;
comment|// CompactRequest object creation time.
specifier|private
name|long
name|selectionTime
decl_stmt|;
comment|// System time used to compare objects in FIFO order. TODO: maybe use selectionTime?
specifier|private
name|Long
name|timeInNanos
decl_stmt|;
specifier|private
name|String
name|regionName
init|=
literal|""
decl_stmt|;
specifier|private
name|String
name|storeName
init|=
literal|""
decl_stmt|;
specifier|private
name|long
name|totalSize
init|=
operator|-
literal|1L
decl_stmt|;
specifier|private
name|Boolean
name|retainDeleteMarkers
init|=
literal|null
decl_stmt|;
comment|/**    * This ctor should be used by coprocessors that want to subclass CompactionRequest.    */
specifier|public
name|CompactionRequest
parameter_list|()
block|{
name|this
operator|.
name|selectionTime
operator|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
expr_stmt|;
name|this
operator|.
name|timeInNanos
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
expr_stmt|;
block|}
specifier|public
name|CompactionRequest
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|files
parameter_list|)
block|{
name|this
argument_list|()
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|files
argument_list|)
expr_stmt|;
name|this
operator|.
name|filesToCompact
operator|=
name|files
expr_stmt|;
name|recalculateSize
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|updateFiles
parameter_list|(
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|files
parameter_list|)
block|{
name|this
operator|.
name|filesToCompact
operator|=
name|files
expr_stmt|;
block|}
comment|/**    * Called before compaction is executed by CompactSplitThread; for use by coproc subclasses.    */
specifier|public
name|void
name|beforeExecute
parameter_list|()
block|{}
comment|/**    * Called after compaction is executed by CompactSplitThread; for use by coproc subclasses.    */
specifier|public
name|void
name|afterExecute
parameter_list|()
block|{}
comment|/**    * Combines the request with other request. Coprocessors subclassing CR may override    * this if they want to do clever things based on CompactionPolicy selection that    * is passed to this method via "other". The default implementation just does a copy.    * @param other Request to combine with.    * @return The result (may be "this" or "other").    */
specifier|public
name|CompactionRequest
name|combineWith
parameter_list|(
name|CompactionRequest
name|other
parameter_list|)
block|{
name|this
operator|.
name|filesToCompact
operator|=
operator|new
name|ArrayList
argument_list|<
name|StoreFile
argument_list|>
argument_list|(
name|other
operator|.
name|getFiles
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|isOffPeak
operator|=
name|other
operator|.
name|isOffPeak
expr_stmt|;
name|this
operator|.
name|isMajor
operator|=
name|other
operator|.
name|isMajor
expr_stmt|;
name|this
operator|.
name|priority
operator|=
name|other
operator|.
name|priority
expr_stmt|;
name|this
operator|.
name|selectionTime
operator|=
name|other
operator|.
name|selectionTime
expr_stmt|;
name|this
operator|.
name|timeInNanos
operator|=
name|other
operator|.
name|timeInNanos
expr_stmt|;
name|this
operator|.
name|regionName
operator|=
name|other
operator|.
name|regionName
expr_stmt|;
name|this
operator|.
name|storeName
operator|=
name|other
operator|.
name|storeName
expr_stmt|;
name|this
operator|.
name|totalSize
operator|=
name|other
operator|.
name|totalSize
expr_stmt|;
name|recalculateSize
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**    * This function will define where in the priority queue the request will    * end up.  Those with the highest priorities will be first.  When the    * priorities are the same it will first compare priority then date    * to maintain a FIFO functionality.    *    *<p>Note: The enqueue timestamp is accurate to the nanosecond. if two    * requests have same timestamp then this function will break the tie    * arbitrarily with hashCode() comparing.    */
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|CompactionRequest
name|request
parameter_list|)
block|{
comment|//NOTE: The head of the priority queue is the least element
if|if
condition|(
name|this
operator|.
name|equals
argument_list|(
name|request
argument_list|)
condition|)
block|{
return|return
literal|0
return|;
comment|//they are the same request
block|}
name|int
name|compareVal
decl_stmt|;
name|compareVal
operator|=
name|priority
operator|-
name|request
operator|.
name|priority
expr_stmt|;
comment|//compare priority
if|if
condition|(
name|compareVal
operator|!=
literal|0
condition|)
block|{
return|return
name|compareVal
return|;
block|}
name|compareVal
operator|=
name|timeInNanos
operator|.
name|compareTo
argument_list|(
name|request
operator|.
name|timeInNanos
argument_list|)
expr_stmt|;
if|if
condition|(
name|compareVal
operator|!=
literal|0
condition|)
block|{
return|return
name|compareVal
return|;
block|}
comment|// break the tie based on hash code
return|return
name|this
operator|.
name|hashCode
argument_list|()
operator|-
name|request
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
return|return
operator|(
name|this
operator|==
name|obj
operator|)
return|;
block|}
specifier|public
name|Collection
argument_list|<
name|StoreFile
argument_list|>
name|getFiles
parameter_list|()
block|{
return|return
name|this
operator|.
name|filesToCompact
return|;
block|}
comment|/**    * Sets the region/store name, for logging.    */
specifier|public
name|void
name|setDescription
parameter_list|(
name|String
name|regionName
parameter_list|,
name|String
name|storeName
parameter_list|)
block|{
name|this
operator|.
name|regionName
operator|=
name|regionName
expr_stmt|;
name|this
operator|.
name|storeName
operator|=
name|storeName
expr_stmt|;
block|}
comment|/** Gets the total size of all StoreFiles in compaction */
specifier|public
name|long
name|getSize
parameter_list|()
block|{
return|return
name|totalSize
return|;
block|}
specifier|public
name|boolean
name|isAllFiles
parameter_list|()
block|{
return|return
name|this
operator|.
name|isMajor
operator|==
name|DisplayCompactionType
operator|.
name|MAJOR
operator|||
name|this
operator|.
name|isMajor
operator|==
name|DisplayCompactionType
operator|.
name|ALL_FILES
return|;
block|}
specifier|public
name|boolean
name|isMajor
parameter_list|()
block|{
return|return
name|this
operator|.
name|isMajor
operator|==
name|DisplayCompactionType
operator|.
name|MAJOR
return|;
block|}
comment|/** Gets the priority for the request */
specifier|public
name|int
name|getPriority
parameter_list|()
block|{
return|return
name|priority
return|;
block|}
comment|/** Sets the priority for the request */
specifier|public
name|void
name|setPriority
parameter_list|(
name|int
name|p
parameter_list|)
block|{
name|this
operator|.
name|priority
operator|=
name|p
expr_stmt|;
block|}
specifier|public
name|boolean
name|isOffPeak
parameter_list|()
block|{
return|return
name|this
operator|.
name|isOffPeak
return|;
block|}
specifier|public
name|void
name|setOffPeak
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|this
operator|.
name|isOffPeak
operator|=
name|value
expr_stmt|;
block|}
specifier|public
name|long
name|getSelectionTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|selectionTime
return|;
block|}
comment|/**    * Specify if this compaction should be a major compaction based on the state of the store    * @param isMajor<tt>true</tt> if the system determines that this compaction should be a major    *          compaction    */
specifier|public
name|void
name|setIsMajor
parameter_list|(
name|boolean
name|isMajor
parameter_list|,
name|boolean
name|isAllFiles
parameter_list|)
block|{
assert|assert
name|isAllFiles
operator|||
operator|!
name|isMajor
assert|;
name|this
operator|.
name|isMajor
operator|=
operator|!
name|isAllFiles
condition|?
name|DisplayCompactionType
operator|.
name|MINOR
else|:
operator|(
name|isMajor
condition|?
name|DisplayCompactionType
operator|.
name|MAJOR
else|:
name|DisplayCompactionType
operator|.
name|ALL_FILES
operator|)
expr_stmt|;
block|}
comment|/**    * Forcefully setting that this compaction has to retain the delete markers in the new compacted    * file, whatever be the type of the compaction.<br>    * Note : By default HBase drops delete markers when the compaction is on all files.    */
specifier|public
name|void
name|forceRetainDeleteMarkers
parameter_list|()
block|{
name|this
operator|.
name|retainDeleteMarkers
operator|=
name|Boolean
operator|.
name|TRUE
expr_stmt|;
block|}
comment|/**    * @return Whether the compaction has to retain the delete markers or not.    */
specifier|public
name|boolean
name|isRetainDeleteMarkers
parameter_list|()
block|{
return|return
operator|(
name|this
operator|.
name|retainDeleteMarkers
operator|!=
literal|null
operator|)
condition|?
name|this
operator|.
name|retainDeleteMarkers
operator|.
name|booleanValue
argument_list|()
else|:
operator|!
name|isAllFiles
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|fsList
init|=
name|Joiner
operator|.
name|on
argument_list|(
literal|", "
argument_list|)
operator|.
name|join
argument_list|(
name|Collections2
operator|.
name|transform
argument_list|(
name|Collections2
operator|.
name|filter
argument_list|(
name|this
operator|.
name|getFiles
argument_list|()
argument_list|,
operator|new
name|Predicate
argument_list|<
name|StoreFile
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|StoreFile
name|sf
parameter_list|)
block|{
return|return
name|sf
operator|.
name|getReader
argument_list|()
operator|!=
literal|null
return|;
block|}
block|}
argument_list|)
argument_list|,
operator|new
name|Function
argument_list|<
name|StoreFile
argument_list|,
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|String
name|apply
parameter_list|(
name|StoreFile
name|sf
parameter_list|)
block|{
return|return
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
operator|(
name|sf
operator|.
name|getReader
argument_list|()
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|sf
operator|.
name|getReader
argument_list|()
operator|.
name|length
argument_list|()
argument_list|)
return|;
block|}
block|}
argument_list|)
argument_list|)
decl_stmt|;
return|return
literal|"regionName="
operator|+
name|regionName
operator|+
literal|", storeName="
operator|+
name|storeName
operator|+
literal|", fileCount="
operator|+
name|this
operator|.
name|getFiles
argument_list|()
operator|.
name|size
argument_list|()
operator|+
literal|", fileSize="
operator|+
name|StringUtils
operator|.
name|humanReadableInt
argument_list|(
name|totalSize
argument_list|)
operator|+
operator|(
operator|(
name|fsList
operator|.
name|isEmpty
argument_list|()
operator|)
condition|?
literal|""
else|:
literal|" ("
operator|+
name|fsList
operator|+
literal|")"
operator|)
operator|+
literal|", priority="
operator|+
name|priority
operator|+
literal|", time="
operator|+
name|timeInNanos
return|;
block|}
comment|/**    * Recalculate the size of the compaction based on current files.    * @param files files that should be included in the compaction    */
specifier|private
name|void
name|recalculateSize
parameter_list|()
block|{
name|long
name|sz
init|=
literal|0
decl_stmt|;
for|for
control|(
name|StoreFile
name|sf
range|:
name|this
operator|.
name|filesToCompact
control|)
block|{
name|Reader
name|r
init|=
name|sf
operator|.
name|getReader
argument_list|()
decl_stmt|;
name|sz
operator|+=
name|r
operator|==
literal|null
condition|?
literal|0
else|:
name|r
operator|.
name|length
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|totalSize
operator|=
name|sz
expr_stmt|;
block|}
block|}
end_class

end_unit

