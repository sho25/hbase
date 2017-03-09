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
name|util
operator|.
name|hbck
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
name|Collection
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
name|HBaseFsck
operator|.
name|HbckInfo
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
name|HBaseFsck
operator|.
name|TableInfo
import|;
end_import

begin_comment
comment|/**  * This interface provides callbacks for handling particular table integrity  * invariant violations.  This could probably be boiled down to handling holes  * and handling overlaps but currently preserves the older more specific error  * condition codes.  */
end_comment

begin_interface
specifier|public
interface|interface
name|TableIntegrityErrorHandler
block|{
name|TableInfo
name|getTableInfo
parameter_list|()
function_decl|;
comment|/**    * Set the TableInfo used by all HRegionInfos fabricated by other callbacks    */
name|void
name|setTableInfo
parameter_list|(
name|TableInfo
name|ti
parameter_list|)
function_decl|;
comment|/**    * Callback for handling case where a Table has a first region that does not    * have an empty start key.    *    * @param hi An HbckInfo of the second region in a table.  This should have    *    a non-empty startkey, and can be used to fabricate a first region that    *    has an empty start key.    */
name|void
name|handleRegionStartKeyNotEmpty
parameter_list|(
name|HbckInfo
name|hi
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Callback for handling case where a Table has a last region that does not    * have an empty end key.    *    * @param curEndKey The end key of the current last region. There should be a new region    *    with start key as this and an empty end key.    */
name|void
name|handleRegionEndKeyNotEmpty
parameter_list|(
name|byte
index|[]
name|curEndKey
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Callback for handling a region that has the same start and end key.    *    * @param hi An HbckInfo for a degenerate key.    */
name|void
name|handleDegenerateRegion
parameter_list|(
name|HbckInfo
name|hi
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Callback for handling two regions that have the same start key.  This is    * a specific case of a region overlap.    * @param hi1 one of the overlapping HbckInfo     * @param hi2 the other overlapping HbckInfo    */
name|void
name|handleDuplicateStartKeys
parameter_list|(
name|HbckInfo
name|hi1
parameter_list|,
name|HbckInfo
name|hi2
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Callback for handling two regions that have the same regionID    * a specific case of a split    * @param hi1 one of the overlapping HbckInfo    * @param hi2 the other overlapping HbckInfo    */
name|void
name|handleSplit
parameter_list|(
name|HbckInfo
name|hi1
parameter_list|,
name|HbckInfo
name|hi2
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Callback for handling two reigons that overlap in some arbitrary way.    * This is a specific case of region overlap, and called for each possible    * pair. If two regions have the same start key, the handleDuplicateStartKeys    * method is called.    * @param hi1 one of the overlapping HbckInfo    * @param hi2 the other overlapping HbckInfo    */
name|void
name|handleOverlapInRegionChain
parameter_list|(
name|HbckInfo
name|hi1
parameter_list|,
name|HbckInfo
name|hi2
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Callback for handling a region hole between two keys.    * @param holeStartKey key at the beginning of the region hole    * @param holeEndKey key at the end of the region hole        */
name|void
name|handleHoleInRegionChain
parameter_list|(
name|byte
index|[]
name|holeStartKey
parameter_list|,
name|byte
index|[]
name|holeEndKey
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Callback for handling an group of regions that overlap.    * @param overlap Collection of overlapping regions.    */
name|void
name|handleOverlapGroup
parameter_list|(
name|Collection
argument_list|<
name|HbckInfo
argument_list|>
name|overlap
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

