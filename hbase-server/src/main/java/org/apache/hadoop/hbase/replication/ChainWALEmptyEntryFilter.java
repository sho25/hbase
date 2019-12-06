begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *<p>  * http://www.apache.org/licenses/LICENSE-2.0  *<p>  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|replication
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
name|HBaseInterfaceAudience
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
name|wal
operator|.
name|WAL
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * A {@link ChainWALEntryFilter} for providing more flexible options  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|REPLICATION
argument_list|)
specifier|public
class|class
name|ChainWALEmptyEntryFilter
extends|extends
name|ChainWALEntryFilter
block|{
specifier|private
name|boolean
name|filterEmptyEntry
init|=
literal|false
decl_stmt|;
specifier|public
name|ChainWALEmptyEntryFilter
parameter_list|(
specifier|final
name|WALEntryFilter
modifier|...
name|filters
parameter_list|)
block|{
name|super
argument_list|(
name|filters
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ChainWALEmptyEntryFilter
parameter_list|(
specifier|final
name|List
argument_list|<
name|WALEntryFilter
argument_list|>
name|filters
parameter_list|)
block|{
name|super
argument_list|(
name|filters
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|WAL
operator|.
name|Entry
name|filter
parameter_list|(
name|WAL
operator|.
name|Entry
name|entry
parameter_list|)
block|{
name|entry
operator|=
name|super
operator|.
name|filter
argument_list|(
name|entry
argument_list|)
expr_stmt|;
if|if
condition|(
name|filterEmptyEntry
operator|&&
name|entry
operator|!=
literal|null
operator|&&
name|entry
operator|.
name|getEdit
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|entry
return|;
block|}
comment|/**    * To allow the empty entries to get filtered, we want to set this optional flag to decide    * if we want to filter the entries which have no cells or all cells got filtered    * though {@link WALCellFilter}.    *    * @param filterEmptyEntry flag    */
annotation|@
name|VisibleForTesting
specifier|public
name|void
name|setFilterEmptyEntry
parameter_list|(
specifier|final
name|boolean
name|filterEmptyEntry
parameter_list|)
block|{
name|this
operator|.
name|filterEmptyEntry
operator|=
name|filterEmptyEntry
expr_stmt|;
block|}
block|}
end_class

end_unit

