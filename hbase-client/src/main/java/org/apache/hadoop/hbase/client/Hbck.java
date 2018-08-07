begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
import|;
end_import

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
name|Abortable
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
name|HBaseInterfaceAudience
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
comment|/**  * Hbck APIs for HBase. Obtain an instance from {@link ClusterConnection#getHbck()} and call  * {@link #close()} when done.  *<p>Hbck client APIs will be mostly used by hbck tool which in turn can be used by operators to  * fix HBase and bringging it to consistent state.</p>  *  * @see ConnectionFactory  * @see ClusterConnection  * @since 2.2.0  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|HBCK
argument_list|)
specifier|public
interface|interface
name|Hbck
extends|extends
name|Abortable
extends|,
name|Closeable
block|{
comment|/**    * Update table state in Meta only. No procedures are submitted to open/ assign or close/    * unassign regions of the table. This is useful only when some procedures/ actions are stuck    * beause of inconsistency between region and table states.    *    * NOTE: This is a dangerous action, as existing running procedures for the table or regions    * which belong to the table may get confused.    *    * @param state table state    * @return previous state of the table in Meta    */
name|TableState
name|setTableStateInMeta
parameter_list|(
name|TableState
name|state
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

