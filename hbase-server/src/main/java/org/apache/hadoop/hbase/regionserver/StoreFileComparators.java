begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|ToLongFunction
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

begin_comment
comment|/**  * Useful comparators for comparing store files.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|final
class|class
name|StoreFileComparators
block|{
comment|/**    * Comparator that compares based on the Sequence Ids of the the store files. Bulk loads that did    * not request a seq ID are given a seq id of -1; thus, they are placed before all non- bulk    * loads, and bulk loads with sequence Id. Among these files, the size is used to determine the    * ordering, then bulkLoadTime. If there are ties, the path name is used as a tie-breaker.    */
specifier|public
specifier|static
specifier|final
name|Comparator
argument_list|<
name|HStoreFile
argument_list|>
name|SEQ_ID
init|=
name|Comparator
operator|.
name|comparingLong
argument_list|(
name|HStoreFile
operator|::
name|getMaxSequenceId
argument_list|)
operator|.
name|thenComparing
argument_list|(
name|Comparator
operator|.
name|comparingLong
argument_list|(
operator|new
name|GetFileSize
argument_list|()
argument_list|)
operator|.
name|reversed
argument_list|()
argument_list|)
operator|.
name|thenComparingLong
argument_list|(
operator|new
name|GetBulkTime
argument_list|()
argument_list|)
operator|.
name|thenComparing
argument_list|(
operator|new
name|GetPathName
argument_list|()
argument_list|)
decl_stmt|;
comment|/**    * Comparator for time-aware compaction. SeqId is still the first ordering criterion to maintain    * MVCC.    */
specifier|public
specifier|static
specifier|final
name|Comparator
argument_list|<
name|HStoreFile
argument_list|>
name|SEQ_ID_MAX_TIMESTAMP
init|=
name|Comparator
operator|.
name|comparingLong
argument_list|(
name|HStoreFile
operator|::
name|getMaxSequenceId
argument_list|)
operator|.
name|thenComparingLong
argument_list|(
operator|new
name|GetMaxTimestamp
argument_list|()
argument_list|)
operator|.
name|thenComparing
argument_list|(
name|Comparator
operator|.
name|comparingLong
argument_list|(
operator|new
name|GetFileSize
argument_list|()
argument_list|)
operator|.
name|reversed
argument_list|()
argument_list|)
operator|.
name|thenComparingLong
argument_list|(
operator|new
name|GetBulkTime
argument_list|()
argument_list|)
operator|.
name|thenComparing
argument_list|(
operator|new
name|GetPathName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
class|class
name|GetFileSize
implements|implements
name|ToLongFunction
argument_list|<
name|HStoreFile
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|long
name|applyAsLong
parameter_list|(
name|HStoreFile
name|sf
parameter_list|)
block|{
if|if
condition|(
name|sf
operator|.
name|getReader
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|sf
operator|.
name|getReader
argument_list|()
operator|.
name|length
argument_list|()
return|;
block|}
else|else
block|{
comment|// the reader may be null for the compacted files and if the archiving
comment|// had failed.
return|return
operator|-
literal|1L
return|;
block|}
block|}
block|}
specifier|private
specifier|static
class|class
name|GetBulkTime
implements|implements
name|ToLongFunction
argument_list|<
name|HStoreFile
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|long
name|applyAsLong
parameter_list|(
name|HStoreFile
name|sf
parameter_list|)
block|{
return|return
name|sf
operator|.
name|getBulkLoadTimestamp
argument_list|()
operator|.
name|orElse
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|GetPathName
implements|implements
name|Function
argument_list|<
name|HStoreFile
argument_list|,
name|String
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|String
name|apply
parameter_list|(
name|HStoreFile
name|sf
parameter_list|)
block|{
return|return
name|sf
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
return|;
block|}
block|}
specifier|private
specifier|static
class|class
name|GetMaxTimestamp
implements|implements
name|ToLongFunction
argument_list|<
name|HStoreFile
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|long
name|applyAsLong
parameter_list|(
name|HStoreFile
name|sf
parameter_list|)
block|{
return|return
name|sf
operator|.
name|getMaximumTimestamp
argument_list|()
operator|.
name|orElse
argument_list|(
name|Long
operator|.
name|MAX_VALUE
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

