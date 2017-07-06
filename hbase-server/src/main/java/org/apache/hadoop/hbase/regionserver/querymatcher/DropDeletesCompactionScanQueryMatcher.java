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
operator|.
name|querymatcher
package|;
end_package

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
name|KeepDeletedCells
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
name|regionserver
operator|.
name|ScanInfo
import|;
end_import

begin_comment
comment|/**  * A query matcher for compaction which can drop delete markers.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|DropDeletesCompactionScanQueryMatcher
extends|extends
name|CompactionScanQueryMatcher
block|{
comment|/**    * By default, when hbase.hstore.time.to.purge.deletes is 0ms, a delete marker is always removed    * during a major compaction. If set to non-zero value then major compaction will try to keep a    * delete marker around for the given number of milliseconds. We want to keep the delete markers    * around a bit longer because old puts might appear out-of-order. For example, during log    * replication between two clusters.    *<p>    * If the delete marker has lived longer than its column-family's TTL then the delete marker will    * be removed even if time.to.purge.deletes has not passed. This is because all the Puts that this    * delete marker can influence would have also expired. (Removing of delete markers on col family    * TTL will not happen if min-versions is set to non-zero)    *<p>    * But, if time.to.purge.deletes has not expired then a delete marker will not be removed just    * because there are no Puts that it is currently influencing. This is because Puts, that this    * delete can influence. may appear out of order.    */
specifier|protected
specifier|final
name|long
name|timeToPurgeDeletes
decl_stmt|;
comment|/**    * Oldest put in any of the involved store files Used to decide whether it is ok to delete family    * delete marker of this store keeps deleted KVs.    */
specifier|protected
specifier|final
name|long
name|earliestPutTs
decl_stmt|;
specifier|protected
name|DropDeletesCompactionScanQueryMatcher
parameter_list|(
name|ScanInfo
name|scanInfo
parameter_list|,
name|DeleteTracker
name|deletes
parameter_list|,
name|long
name|readPointToUse
parameter_list|,
name|long
name|earliestPutTs
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|,
name|long
name|now
parameter_list|)
block|{
name|super
argument_list|(
name|scanInfo
argument_list|,
name|deletes
argument_list|,
name|readPointToUse
argument_list|,
name|oldestUnexpiredTS
argument_list|,
name|now
argument_list|)
expr_stmt|;
name|this
operator|.
name|timeToPurgeDeletes
operator|=
name|scanInfo
operator|.
name|getTimeToPurgeDeletes
argument_list|()
expr_stmt|;
name|this
operator|.
name|earliestPutTs
operator|=
name|earliestPutTs
expr_stmt|;
block|}
specifier|protected
specifier|final
name|MatchCode
name|tryDropDelete
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
name|long
name|timestamp
init|=
name|cell
operator|.
name|getTimestamp
argument_list|()
decl_stmt|;
comment|// If it is not the time to drop the delete marker, just return
if|if
condition|(
name|timeToPurgeDeletes
operator|>
literal|0
operator|&&
name|now
operator|-
name|timestamp
operator|<=
name|timeToPurgeDeletes
condition|)
block|{
return|return
name|MatchCode
operator|.
name|INCLUDE
return|;
block|}
if|if
condition|(
name|keepDeletedCells
operator|==
name|KeepDeletedCells
operator|.
name|TRUE
operator|||
operator|(
name|keepDeletedCells
operator|==
name|KeepDeletedCells
operator|.
name|TTL
operator|&&
name|timestamp
operator|>=
name|oldestUnexpiredTS
operator|)
condition|)
block|{
comment|// If keepDeletedCell is true, or the delete marker is not expired yet, we should include it
comment|// in version counting to see if we can drop it. The only exception is that, we can make
comment|// sure that no put is older than this delete marker. And under this situation, all later
comment|// cells of this column(must be delete markers) can be skipped.
if|if
condition|(
name|timestamp
operator|<
name|earliestPutTs
condition|)
block|{
return|return
name|columns
operator|.
name|getNextRowOrNextColumn
argument_list|(
name|cell
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
else|else
block|{
return|return
name|MatchCode
operator|.
name|SKIP
return|;
block|}
block|}
block|}
end_class

end_unit

