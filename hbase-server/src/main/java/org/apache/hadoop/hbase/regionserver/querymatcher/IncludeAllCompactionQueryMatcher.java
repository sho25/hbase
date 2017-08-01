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
name|regionserver
operator|.
name|ScanInfo
import|;
end_import

begin_comment
comment|/**  * A compaction query matcher that always return INCLUDE and drops nothing.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|IncludeAllCompactionQueryMatcher
extends|extends
name|MinorCompactionScanQueryMatcher
block|{
specifier|public
name|IncludeAllCompactionQueryMatcher
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
return|return
name|MatchCode
operator|.
name|INCLUDE
return|;
block|}
block|}
end_class

end_unit
