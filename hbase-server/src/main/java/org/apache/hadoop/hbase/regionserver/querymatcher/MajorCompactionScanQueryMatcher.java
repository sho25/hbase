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
name|java
operator|.
name|io
operator|.
name|IOException
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
name|ScanInfo
import|;
end_import

begin_comment
comment|/**  * Query matcher for major compaction.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MajorCompactionScanQueryMatcher
extends|extends
name|DropDeletesCompactionScanQueryMatcher
block|{
specifier|public
name|MajorCompactionScanQueryMatcher
parameter_list|(
name|ScanInfo
name|scanInfo
parameter_list|,
name|DeleteTracker
name|deletes
parameter_list|,
name|ColumnTracker
name|columns
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
name|columns
argument_list|,
name|readPointToUse
argument_list|,
name|earliestPutTs
argument_list|,
name|oldestUnexpiredTS
argument_list|,
name|now
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|MatchCode
name|match
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
name|MatchCode
name|returnCode
init|=
name|preCheck
argument_list|(
name|cell
argument_list|)
decl_stmt|;
if|if
condition|(
name|returnCode
operator|!=
literal|null
condition|)
block|{
return|return
name|returnCode
return|;
block|}
name|long
name|timestamp
init|=
name|cell
operator|.
name|getTimestamp
argument_list|()
decl_stmt|;
name|long
name|mvccVersion
init|=
name|cell
operator|.
name|getSequenceId
argument_list|()
decl_stmt|;
name|byte
name|typeByte
init|=
name|cell
operator|.
name|getTypeByte
argument_list|()
decl_stmt|;
comment|// The delete logic is pretty complicated now.
comment|// This is corroborated by the following:
comment|// 1. The store might be instructed to keep deleted rows around.
comment|// 2. A scan can optionally see past a delete marker now.
comment|// 3. If deleted rows are kept, we have to find out when we can
comment|// remove the delete markers.
comment|// 4. Family delete markers are always first (regardless of their TS)
comment|// 5. Delete markers should not be counted as version
comment|// 6. Delete markers affect puts of the *same* TS
comment|// 7. Delete marker need to be version counted together with puts
comment|// they affect
comment|//
if|if
condition|(
name|PrivateCellUtil
operator|.
name|isDelete
argument_list|(
name|typeByte
argument_list|)
condition|)
block|{
if|if
condition|(
name|mvccVersion
operator|>
name|maxReadPointToTrackVersions
condition|)
block|{
comment|// We can not drop this delete marker yet, and also we should not use this delete marker to
comment|// mask any cell yet.
return|return
name|MatchCode
operator|.
name|INCLUDE
return|;
block|}
name|trackDelete
argument_list|(
name|cell
argument_list|)
expr_stmt|;
name|returnCode
operator|=
name|tryDropDelete
argument_list|(
name|cell
argument_list|)
expr_stmt|;
if|if
condition|(
name|returnCode
operator|!=
literal|null
condition|)
block|{
return|return
name|returnCode
return|;
block|}
block|}
else|else
block|{
name|returnCode
operator|=
name|checkDeleted
argument_list|(
name|deletes
argument_list|,
name|cell
argument_list|)
expr_stmt|;
if|if
condition|(
name|returnCode
operator|!=
literal|null
condition|)
block|{
return|return
name|returnCode
return|;
block|}
block|}
comment|// Skip checking column since we do not remove column during compaction.
return|return
name|columns
operator|.
name|checkVersions
argument_list|(
name|cell
argument_list|,
name|timestamp
argument_list|,
name|typeByte
argument_list|,
name|mvccVersion
operator|>
name|maxReadPointToTrackVersions
argument_list|)
return|;
block|}
block|}
end_class

end_unit

