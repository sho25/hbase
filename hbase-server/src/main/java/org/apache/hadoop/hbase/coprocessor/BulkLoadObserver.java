begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|coprocessor
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
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Coprocessors implement this interface to observe and mediate bulk load operations.  *<br><br>  *  *<h3>Exception Handling</h3>  * For all functions, exception handling is done as follows:  *<ul>  *<li>Exceptions of type {@link IOException} are reported back to client.</li>  *<li>For any other kind of exception:  *<ul>  *<li>If the configuration {@link CoprocessorHost#ABORT_ON_ERROR_KEY} is set to true, then  *         the server aborts.</li>  *<li>Otherwise, coprocessor is removed from the server and  *         {@link org.apache.hadoop.hbase.DoNotRetryIOException} is returned to the client.</li>  *</ul>  *</li>  *</ul>  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|BulkLoadObserver
block|{
comment|/**       * Called as part of SecureBulkLoadEndpoint.prepareBulkLoad() RPC call.       * It can't bypass the default action, e.g., ctx.bypass() won't have effect.       * If you need to get the region or table name, get it from the       *<code>ctx</code> as follows:<code>code>ctx.getEnvironment().getRegion()</code>. Use       * getRegionInfo to fetch the encodedName and use getTableDescriptor() to get the tableName.       * @param ctx the environment to interact with the framework and master       */
specifier|default
name|void
name|prePrepareBulkLoad
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{}
comment|/**       * Called as part of SecureBulkLoadEndpoint.cleanupBulkLoad() RPC call.       * It can't bypass the default action, e.g., ctx.bypass() won't have effect.       * If you need to get the region or table name, get it from the       *<code>ctx</code> as follows:<code>code>ctx.getEnvironment().getRegion()</code>. Use       * getRegionInfo to fetch the encodedName and use getTableDescriptor() to get the tableName.       * @param ctx the environment to interact with the framework and master       */
specifier|default
name|void
name|preCleanupBulkLoad
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{}
block|}
end_interface

end_unit

