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
name|coordination
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
name|HRegionInfo
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
name|Server
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
name|ServerName
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
name|RegionServerServices
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
name|SplitTransaction
import|;
end_import

begin_comment
comment|/**  * Coordination operations for split transaction. The split operation should be coordinated at the  * following stages:  * 1. start - all preparation/initialization for split transaction should be done there.  * 2. waitForSplitTransaction  - the coordination should perform all logic related to split  *    transaction and wait till it's finished  * 3. completeSplitTransaction - all steps that are required to complete the transaction.  *    Called after PONR (point of no return)  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|SplitTransactionCoordination
block|{
comment|/**    * Dummy interface for split transaction details.    */
specifier|public
specifier|static
interface|interface
name|SplitTransactionDetails
block|{   }
name|SplitTransactionDetails
name|getDefaultDetails
parameter_list|()
function_decl|;
comment|/**    * init coordination for split transaction    * @param parent region to be created as offline    * @param serverName server event originates from    * @param hri_a daughter region    * @param hri_b daughter region    * @throws IOException    */
name|void
name|startSplitTransaction
parameter_list|(
name|HRegion
name|parent
parameter_list|,
name|ServerName
name|serverName
parameter_list|,
name|HRegionInfo
name|hri_a
parameter_list|,
name|HRegionInfo
name|hri_b
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Wait while coordination process the transaction    * @param services Used to online/offline regions.    * @param parent region    * @param hri_a daughter region    * @param hri_b daughter region    * @param std split transaction details    * @throws IOException    */
name|void
name|waitForSplitTransaction
parameter_list|(
specifier|final
name|RegionServerServices
name|services
parameter_list|,
name|HRegion
name|parent
parameter_list|,
name|HRegionInfo
name|hri_a
parameter_list|,
name|HRegionInfo
name|hri_b
parameter_list|,
name|SplitTransactionDetails
name|std
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Finish off split transaction    * @param services Used to online/offline regions.    * @param first daughter region    * @param second daughter region    * @param std split transaction details    * @param parent    * @throws IOException If thrown, transaction failed. Call    *           {@link SplitTransaction#rollback(Server, RegionServerServices)}    */
name|void
name|completeSplitTransaction
parameter_list|(
name|RegionServerServices
name|services
parameter_list|,
name|HRegion
name|first
parameter_list|,
name|HRegion
name|second
parameter_list|,
name|SplitTransactionDetails
name|std
parameter_list|,
name|HRegion
name|parent
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * clean the split transaction    * @param hri node to delete    */
name|void
name|clean
parameter_list|(
specifier|final
name|HRegionInfo
name|hri
parameter_list|)
function_decl|;
comment|/**    * Required by AssignmentManager    */
name|int
name|processTransition
parameter_list|(
name|HRegionInfo
name|p
parameter_list|,
name|HRegionInfo
name|hri_a
parameter_list|,
name|HRegionInfo
name|hri_b
parameter_list|,
name|ServerName
name|sn
parameter_list|,
name|SplitTransactionDetails
name|std
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

