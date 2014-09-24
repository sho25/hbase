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
name|ipc
package|;
end_package

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
name|CellScannable
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
name|CellScanner
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
name|HConstants
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
name|TableName
import|;
end_import

begin_comment
comment|/**  * Optionally carries Cells across the proxy/service interface down into ipc. On its  * way out it optionally carries a set of result Cell data.  We stick the Cells here when we want  * to avoid having to protobuf them.  This class is used ferrying data across the proxy/protobuf  * service chasm.  Used by client and server ipc'ing.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|PayloadCarryingRpcController
extends|extends
name|TimeLimitedRpcController
implements|implements
name|CellScannable
block|{
comment|/**    * Priority to set on this request.  Set it here in controller so available composing the    * request.  This is the ordained way of setting priorities going forward.  We will be    * undoing the old annotation-based mechanism.    */
comment|// Currently only multi call makes use of this.  Eventually this should be only way to set
comment|// priority.
specifier|private
name|int
name|priority
init|=
literal|0
decl_stmt|;
comment|/**    * They are optionally set on construction, cleared after we make the call, and then optionally    * set on response with the result. We use this lowest common denominator access to Cells because    * sometimes the scanner is backed by a List of Cells and other times, it is backed by an    * encoded block that implements CellScanner.    */
specifier|private
name|CellScanner
name|cellScanner
decl_stmt|;
specifier|public
name|PayloadCarryingRpcController
parameter_list|()
block|{
name|this
argument_list|(
operator|(
name|CellScanner
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|PayloadCarryingRpcController
parameter_list|(
specifier|final
name|CellScanner
name|cellScanner
parameter_list|)
block|{
name|this
operator|.
name|cellScanner
operator|=
name|cellScanner
expr_stmt|;
block|}
specifier|public
name|PayloadCarryingRpcController
parameter_list|(
specifier|final
name|List
argument_list|<
name|CellScannable
argument_list|>
name|cellIterables
parameter_list|)
block|{
name|this
operator|.
name|cellScanner
operator|=
name|cellIterables
operator|==
literal|null
condition|?
literal|null
else|:
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|cellIterables
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return One-shot cell scanner (you cannot back it up and restart)    */
specifier|public
name|CellScanner
name|cellScanner
parameter_list|()
block|{
return|return
name|cellScanner
return|;
block|}
specifier|public
name|void
name|setCellScanner
parameter_list|(
specifier|final
name|CellScanner
name|cellScanner
parameter_list|)
block|{
name|this
operator|.
name|cellScanner
operator|=
name|cellScanner
expr_stmt|;
block|}
comment|/**    * @param priority Priority for this request; should fall roughly in the range    * {@link HConstants#NORMAL_QOS} to {@link HConstants#HIGH_QOS}    */
specifier|public
name|void
name|setPriority
parameter_list|(
name|int
name|priority
parameter_list|)
block|{
name|this
operator|.
name|priority
operator|=
name|priority
expr_stmt|;
block|}
comment|/**    * @param tn Set priority based off the table we are going against.    */
specifier|public
name|void
name|setPriority
parameter_list|(
specifier|final
name|TableName
name|tn
parameter_list|)
block|{
name|this
operator|.
name|priority
operator|=
name|tn
operator|!=
literal|null
operator|&&
name|tn
operator|.
name|isSystemTable
argument_list|()
condition|?
name|HConstants
operator|.
name|HIGH_QOS
else|:
name|HConstants
operator|.
name|NORMAL_QOS
expr_stmt|;
block|}
comment|/**    * @return The priority of this request    */
specifier|public
name|int
name|getPriority
parameter_list|()
block|{
return|return
name|priority
return|;
block|}
block|}
end_class

end_unit

