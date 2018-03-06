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
name|replication
package|;
end_package

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
operator|.
name|Entry
import|;
end_import

begin_comment
comment|/**  * A Filter for WAL entries before being sent over to replication. Multiple  * filters might be chained together using {@link ChainWALEntryFilter}.  * Applied on the replication source side.  *<p>There is also a filter that can be installed on the sink end of a replication stream.  * See {@link org.apache.hadoop.hbase.replication.regionserver.WALEntrySinkFilter}. Certain  * use-cases may need such a facility but better to filter here on the source side rather  * than later, after the edit arrives at the sink.</p>  * @see org.apache.hadoop.hbase.replication.regionserver.WALEntrySinkFilter for filtering  * replication on the sink-side.  */
end_comment

begin_interface
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
interface|interface
name|WALEntryFilter
block|{
comment|/**    *<p>    * Applies the filter, possibly returning a different Entry instance. If null is returned, the    * entry will be skipped.    *</p>    *<p>    * Notice that you are free to modify the cell list of the give entry, but do not change the    * content of the cell, it may be used by others at the same time(and usually you can not modify a    * cell unless you cast it to the implementation class, which is not a good idea).    *</p>    * @param entry Entry to filter    * @return a (possibly modified) Entry to use. Returning null or an entry with no cells will cause    *         the entry to be skipped for replication.    */
specifier|public
name|Entry
name|filter
parameter_list|(
name|Entry
name|entry
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

