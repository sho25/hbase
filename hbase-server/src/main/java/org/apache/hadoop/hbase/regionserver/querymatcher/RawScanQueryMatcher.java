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
name|client
operator|.
name|Scan
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
comment|/**  * Query matcher for raw scan.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|RawScanQueryMatcher
extends|extends
name|UserScanQueryMatcher
block|{
specifier|protected
name|RawScanQueryMatcher
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|ScanInfo
name|scanInfo
parameter_list|,
name|ColumnTracker
name|columns
parameter_list|,
name|boolean
name|hasNullColumn
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
name|scan
argument_list|,
name|scanInfo
argument_list|,
name|columns
argument_list|,
name|hasNullColumn
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
if|if
condition|(
name|filter
operator|!=
literal|null
operator|&&
name|filter
operator|.
name|filterAllRemaining
argument_list|()
condition|)
block|{
return|return
name|MatchCode
operator|.
name|DONE_SCAN
return|;
block|}
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
name|byte
name|typeByte
init|=
name|cell
operator|.
name|getTypeByte
argument_list|()
decl_stmt|;
comment|// For a raw scan, we do not filter out any cells by delete marker, and delete marker is also
comment|// returned, so we do not need to track delete.
return|return
name|matchColumn
argument_list|(
name|cell
argument_list|,
name|timestamp
argument_list|,
name|typeByte
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|reset
parameter_list|()
block|{   }
annotation|@
name|Override
specifier|protected
name|boolean
name|isGet
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
specifier|static
name|RawScanQueryMatcher
name|create
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|ScanInfo
name|scanInfo
parameter_list|,
name|ColumnTracker
name|columns
parameter_list|,
name|boolean
name|hasNullColumn
parameter_list|,
name|long
name|oldestUnexpiredTS
parameter_list|,
name|long
name|now
parameter_list|)
block|{
if|if
condition|(
name|scan
operator|.
name|isReversed
argument_list|()
condition|)
block|{
if|if
condition|(
name|scan
operator|.
name|includeStopRow
argument_list|()
condition|)
block|{
return|return
operator|new
name|RawScanQueryMatcher
argument_list|(
name|scan
argument_list|,
name|scanInfo
argument_list|,
name|columns
argument_list|,
name|hasNullColumn
argument_list|,
name|oldestUnexpiredTS
argument_list|,
name|now
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|boolean
name|moreRowsMayExistsAfter
parameter_list|(
name|int
name|cmpToStopRow
parameter_list|)
block|{
return|return
name|cmpToStopRow
operator|>=
literal|0
return|;
block|}
block|}
return|;
block|}
else|else
block|{
return|return
operator|new
name|RawScanQueryMatcher
argument_list|(
name|scan
argument_list|,
name|scanInfo
argument_list|,
name|columns
argument_list|,
name|hasNullColumn
argument_list|,
name|oldestUnexpiredTS
argument_list|,
name|now
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|boolean
name|moreRowsMayExistsAfter
parameter_list|(
name|int
name|cmpToStopRow
parameter_list|)
block|{
return|return
name|cmpToStopRow
operator|>
literal|0
return|;
block|}
block|}
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|scan
operator|.
name|includeStopRow
argument_list|()
condition|)
block|{
return|return
operator|new
name|RawScanQueryMatcher
argument_list|(
name|scan
argument_list|,
name|scanInfo
argument_list|,
name|columns
argument_list|,
name|hasNullColumn
argument_list|,
name|oldestUnexpiredTS
argument_list|,
name|now
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|boolean
name|moreRowsMayExistsAfter
parameter_list|(
name|int
name|cmpToStopRow
parameter_list|)
block|{
return|return
name|cmpToStopRow
operator|<=
literal|0
return|;
block|}
block|}
return|;
block|}
else|else
block|{
return|return
operator|new
name|RawScanQueryMatcher
argument_list|(
name|scan
argument_list|,
name|scanInfo
argument_list|,
name|columns
argument_list|,
name|hasNullColumn
argument_list|,
name|oldestUnexpiredTS
argument_list|,
name|now
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|boolean
name|moreRowsMayExistsAfter
parameter_list|(
name|int
name|cmpToStopRow
parameter_list|)
block|{
return|return
name|cmpToStopRow
operator|<
literal|0
return|;
block|}
block|}
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

