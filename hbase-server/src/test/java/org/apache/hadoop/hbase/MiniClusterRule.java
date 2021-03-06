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
name|concurrent
operator|.
name|CompletableFuture
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
name|AsyncConnection
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
name|ConnectionFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|ExternalResource
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|TestRule
import|;
end_import

begin_comment
comment|/**  * A {@link TestRule} that manages an instance of the {@link MiniHBaseCluster}. Can be used in  * either the {@link Rule} or {@link ClassRule} positions. Built on top of an instance of  * {@link HBaseTestingUtility}, so be weary of intermixing direct use of that class with this Rule.  *</p>  * Use in combination with {@link ConnectionRule}, for example:  *  *<pre>{@code  *   public class TestMyClass {  *     @ClassRule  *     public static final MiniClusterRule miniClusterRule = new MiniClusterRule();  *  *     @Rule  *     public final ConnectionRule connectionRule =  *       new ConnectionRule(miniClusterRule::createConnection);  *   }  * }</pre>  */
end_comment

begin_class
specifier|public
class|class
name|MiniClusterRule
extends|extends
name|ExternalResource
block|{
specifier|private
specifier|final
name|HBaseTestingUtility
name|testingUtility
decl_stmt|;
specifier|private
specifier|final
name|StartMiniClusterOption
name|miniClusterOptions
decl_stmt|;
specifier|private
name|MiniHBaseCluster
name|miniCluster
decl_stmt|;
comment|/**    * Create an instance over the default options provided by {@link StartMiniClusterOption}.    */
specifier|public
name|MiniClusterRule
parameter_list|()
block|{
name|this
argument_list|(
name|StartMiniClusterOption
operator|.
name|builder
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an instance using the provided {@link StartMiniClusterOption}.    */
specifier|public
name|MiniClusterRule
parameter_list|(
specifier|final
name|StartMiniClusterOption
name|miniClusterOptions
parameter_list|)
block|{
name|this
operator|.
name|testingUtility
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|this
operator|.
name|miniClusterOptions
operator|=
name|miniClusterOptions
expr_stmt|;
block|}
comment|/**    * @return the underlying instance of {@link HBaseTestingUtility}    */
specifier|public
name|HBaseTestingUtility
name|getTestingUtility
parameter_list|()
block|{
return|return
name|testingUtility
return|;
block|}
comment|/**    * Create a {@link AsyncConnection} to the managed {@link MiniHBaseCluster}. It's up to the caller    * to {@link AsyncConnection#close() close()} the connection when finished.    */
specifier|public
name|CompletableFuture
argument_list|<
name|AsyncConnection
argument_list|>
name|createConnection
parameter_list|()
block|{
if|if
condition|(
name|miniCluster
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"test cluster not initialized"
argument_list|)
throw|;
block|}
return|return
name|ConnectionFactory
operator|.
name|createAsyncConnection
argument_list|(
name|miniCluster
operator|.
name|getConf
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|before
parameter_list|()
throws|throws
name|Throwable
block|{
name|miniCluster
operator|=
name|testingUtility
operator|.
name|startMiniCluster
argument_list|(
name|miniClusterOptions
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|after
parameter_list|()
block|{
try|try
block|{
name|testingUtility
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

