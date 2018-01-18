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
name|regionserver
package|;
end_package

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
comment|/**  * This is a special {@link ScannerContext} subclass that is designed to be used globally when  * limits should not be enforced during invocations of {@link InternalScanner#next(java.util.List)}  * or {@link RegionScanner#next(java.util.List)}.  *<p>  * Instances of {@link NoLimitScannerContext} are immutable after construction. Any attempt to  * change the limits or progress of a {@link NoLimitScannerContext} will fail silently. The net  * effect is that all limit checks will return false, thus indicating that a limit has not been  * reached.  */
end_comment

begin_class
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
class|class
name|NoLimitScannerContext
extends|extends
name|ScannerContext
block|{
specifier|public
name|NoLimitScannerContext
parameter_list|()
block|{
name|super
argument_list|(
literal|false
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Use this instance whenever limits do not need to be enforced.    */
specifier|private
specifier|static
specifier|final
name|ScannerContext
name|NO_LIMIT
init|=
operator|new
name|NoLimitScannerContext
argument_list|()
decl_stmt|;
comment|/**    * @return The static, immutable instance of {@link NoLimitScannerContext} to be used whenever    *         limits should not be enforced    */
specifier|public
specifier|static
specifier|final
name|ScannerContext
name|getInstance
parameter_list|()
block|{
return|return
name|NO_LIMIT
return|;
block|}
annotation|@
name|Override
name|void
name|setKeepProgress
parameter_list|(
name|boolean
name|keepProgress
parameter_list|)
block|{
comment|// Do nothing. NoLimitScannerContext instances are immutable post-construction
block|}
annotation|@
name|Override
name|void
name|setBatchProgress
parameter_list|(
name|int
name|batchProgress
parameter_list|)
block|{
comment|// Do nothing. NoLimitScannerContext instances are immutable post-construction
block|}
annotation|@
name|Override
name|void
name|setSizeProgress
parameter_list|(
name|long
name|sizeProgress
parameter_list|,
name|long
name|heapSizeProgress
parameter_list|)
block|{
comment|// Do nothing. NoLimitScannerContext instances are immutable post-construction
block|}
annotation|@
name|Override
name|void
name|setProgress
parameter_list|(
name|int
name|batchProgress
parameter_list|,
name|long
name|sizeProgress
parameter_list|,
name|long
name|heapSizeProgress
parameter_list|)
block|{
comment|// Do nothing. NoLimitScannerContext instances are immutable post-construction
block|}
annotation|@
name|Override
name|void
name|clearProgress
parameter_list|()
block|{
comment|// Do nothing. NoLimitScannerContext instances are immutable post-construction
block|}
annotation|@
name|Override
name|void
name|setSizeLimitScope
parameter_list|(
name|LimitScope
name|scope
parameter_list|)
block|{
comment|// Do nothing. NoLimitScannerContext instances are immutable post-construction
block|}
annotation|@
name|Override
name|void
name|setTimeLimitScope
parameter_list|(
name|LimitScope
name|scope
parameter_list|)
block|{
comment|// Do nothing. NoLimitScannerContext instances are immutable post-construction
block|}
annotation|@
name|Override
name|NextState
name|setScannerState
parameter_list|(
name|NextState
name|state
parameter_list|)
block|{
comment|// Do nothing. NoLimitScannerContext instances are immutable post-construction
return|return
name|state
return|;
block|}
annotation|@
name|Override
name|boolean
name|checkBatchLimit
parameter_list|(
name|LimitScope
name|checkerScope
parameter_list|)
block|{
comment|// No limits can be specified, thus return false to indicate no limit has been reached.
return|return
literal|false
return|;
block|}
annotation|@
name|Override
name|boolean
name|checkSizeLimit
parameter_list|(
name|LimitScope
name|checkerScope
parameter_list|)
block|{
comment|// No limits can be specified, thus return false to indicate no limit has been reached.
return|return
literal|false
return|;
block|}
annotation|@
name|Override
name|boolean
name|checkTimeLimit
parameter_list|(
name|LimitScope
name|checkerScope
parameter_list|)
block|{
comment|// No limits can be specified, thus return false to indicate no limit has been reached.
return|return
literal|false
return|;
block|}
annotation|@
name|Override
name|boolean
name|checkAnyLimitReached
parameter_list|(
name|LimitScope
name|checkerScope
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

