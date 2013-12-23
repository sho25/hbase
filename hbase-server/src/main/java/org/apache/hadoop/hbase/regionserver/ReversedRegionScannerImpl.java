begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|List
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
name|HRegion
operator|.
name|RegionScannerImpl
import|;
end_import

begin_comment
comment|/**  * ReversibleRegionScannerImpl extends from RegionScannerImpl, and is used to  * support reversed scanning.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|ReversedRegionScannerImpl
extends|extends
name|RegionScannerImpl
block|{
comment|/**    * @param scan    * @param additionalScanners    * @param region    * @throws IOException    */
name|ReversedRegionScannerImpl
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|additionalScanners
parameter_list|,
name|HRegion
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|region
operator|.
name|super
argument_list|(
name|scan
argument_list|,
name|additionalScanners
argument_list|,
name|region
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|initializeKVHeap
parameter_list|(
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|scanners
parameter_list|,
name|List
argument_list|<
name|KeyValueScanner
argument_list|>
name|joinedScanners
parameter_list|,
name|HRegion
name|region
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|storeHeap
operator|=
operator|new
name|ReversedKeyValueHeap
argument_list|(
name|scanners
argument_list|,
name|region
operator|.
name|getComparator
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|joinedScanners
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|this
operator|.
name|joinedHeap
operator|=
operator|new
name|ReversedKeyValueHeap
argument_list|(
name|joinedScanners
argument_list|,
name|region
operator|.
name|getComparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|isStopRow
parameter_list|(
name|byte
index|[]
name|currentRow
parameter_list|,
name|int
name|offset
parameter_list|,
name|short
name|length
parameter_list|)
block|{
return|return
name|currentRow
operator|==
literal|null
operator|||
operator|(
name|super
operator|.
name|stopRow
operator|!=
literal|null
operator|&&
name|region
operator|.
name|getComparator
argument_list|()
operator|.
name|compareRows
argument_list|(
name|stopRow
argument_list|,
literal|0
argument_list|,
name|stopRow
operator|.
name|length
argument_list|,
name|currentRow
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
operator|>=
name|super
operator|.
name|isScan
operator|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|boolean
name|nextRow
parameter_list|(
name|byte
index|[]
name|currentRow
parameter_list|,
name|int
name|offset
parameter_list|,
name|short
name|length
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|super
operator|.
name|joinedContinuationRow
operator|==
literal|null
operator|:
literal|"Trying to go to next row during joinedHeap read."
assert|;
name|byte
name|row
index|[]
init|=
operator|new
name|byte
index|[
name|length
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|currentRow
argument_list|,
name|offset
argument_list|,
name|row
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|this
operator|.
name|storeHeap
operator|.
name|seekToPreviousRow
argument_list|(
name|KeyValue
operator|.
name|createFirstOnRow
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|resetFilters
argument_list|()
expr_stmt|;
comment|// Calling the hook in CP which allows it to do a fast forward
if|if
condition|(
name|this
operator|.
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|this
operator|.
name|region
operator|.
name|getCoprocessorHost
argument_list|()
operator|.
name|postScannerFilterRow
argument_list|(
name|this
argument_list|,
name|currentRow
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

