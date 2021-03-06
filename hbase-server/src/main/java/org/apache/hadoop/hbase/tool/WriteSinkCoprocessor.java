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
name|tool
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
name|Optional
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
name|atomic
operator|.
name|AtomicLong
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
name|client
operator|.
name|Mutation
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
name|coprocessor
operator|.
name|ObserverContext
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
name|coprocessor
operator|.
name|RegionCoprocessor
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
name|coprocessor
operator|.
name|RegionCoprocessorEnvironment
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
name|coprocessor
operator|.
name|RegionObserver
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
name|MiniBatchOperationInProgress
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
name|OperationStatus
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  *<p>  * This coprocessor 'shallows' all the writes. It allows to test a pure  * write workload, going through all the communication layers.  * The reads will work as well, but they as we never write, they will always always  * return an empty structure. The WAL is also skipped.  * Obviously, the region will never be split automatically. It's up to the user  * to split and move it.  *</p>  *<p>  * For a table created like this:  * create 'usertable', {NAME =&gt; 'f1', VERSIONS =&gt; 1}  *</p>  *<p>  * You can then add the coprocessor with this command:  * alter 'usertable', 'coprocessor' =&gt; '|org.apache.hadoop.hbase.tool.WriteSinkCoprocessor|'  *</p>  *<p>  * And then  * put 'usertable', 'f1', 'f1', 'f1'  *</p>  *<p>  * scan 'usertable'  * Will return:  * 0 row(s) in 0.0050 seconds  *</p>  * TODO: It needs tests  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|WriteSinkCoprocessor
implements|implements
name|RegionCoprocessor
implements|,
name|RegionObserver
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|WriteSinkCoprocessor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|AtomicLong
name|ops
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|RegionObserver
argument_list|>
name|getRegionObserver
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|this
argument_list|)
return|;
block|}
specifier|private
name|String
name|regionName
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|preOpen
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|e
parameter_list|)
throws|throws
name|IOException
block|{
name|regionName
operator|=
name|e
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preBatchMutate
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|RegionCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
specifier|final
name|MiniBatchOperationInProgress
argument_list|<
name|Mutation
argument_list|>
name|miniBatchOp
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|ops
operator|.
name|incrementAndGet
argument_list|()
operator|%
literal|20000
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Wrote "
operator|+
name|ops
operator|.
name|get
argument_list|()
operator|+
literal|" times in region "
operator|+
name|regionName
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|miniBatchOp
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|miniBatchOp
operator|.
name|setOperationStatus
argument_list|(
name|i
argument_list|,
operator|new
name|OperationStatus
argument_list|(
name|HConstants
operator|.
name|OperationStatusCode
operator|.
name|SUCCESS
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|c
operator|.
name|bypass
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

