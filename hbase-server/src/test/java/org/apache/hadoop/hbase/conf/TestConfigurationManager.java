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
name|conf
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|Configuration
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
name|testclassification
operator|.
name|ClientTests
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
name|testclassification
operator|.
name|SmallTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|SmallTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestConfigurationManager
block|{
specifier|public
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestConfigurationManager
operator|.
name|class
argument_list|)
decl_stmt|;
class|class
name|DummyConfigurationObserver
implements|implements
name|ConfigurationObserver
block|{
specifier|private
name|boolean
name|notifiedOnChange
init|=
literal|false
decl_stmt|;
specifier|private
name|ConfigurationManager
name|cm
decl_stmt|;
specifier|public
name|DummyConfigurationObserver
parameter_list|(
name|ConfigurationManager
name|cm
parameter_list|)
block|{
name|this
operator|.
name|cm
operator|=
name|cm
expr_stmt|;
name|register
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|onConfigurationChange
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|notifiedOnChange
operator|=
literal|true
expr_stmt|;
block|}
comment|// Was the observer notified on Configuration change?
specifier|public
name|boolean
name|wasNotifiedOnChange
parameter_list|()
block|{
return|return
name|notifiedOnChange
return|;
block|}
specifier|public
name|void
name|resetNotifiedOnChange
parameter_list|()
block|{
name|notifiedOnChange
operator|=
literal|false
expr_stmt|;
block|}
specifier|public
name|void
name|register
parameter_list|()
block|{
name|this
operator|.
name|cm
operator|.
name|registerObserver
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|deregister
parameter_list|()
block|{
name|this
operator|.
name|cm
operator|.
name|deregisterObserver
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Test if observers get notified by the<code>ConfigurationManager</code>    * when the Configuration is reloaded.    */
annotation|@
name|Test
specifier|public
name|void
name|testCheckIfObserversNotified
parameter_list|()
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|ConfigurationManager
name|cm
init|=
operator|new
name|ConfigurationManager
argument_list|()
decl_stmt|;
name|DummyConfigurationObserver
name|d1
init|=
operator|new
name|DummyConfigurationObserver
argument_list|(
name|cm
argument_list|)
decl_stmt|;
comment|// Check if we get notified.
name|cm
operator|.
name|notifyAllObservers
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|d1
operator|.
name|wasNotifiedOnChange
argument_list|()
argument_list|)
expr_stmt|;
name|d1
operator|.
name|resetNotifiedOnChange
argument_list|()
expr_stmt|;
comment|// Now check if we get notified on change with more than one observers.
name|DummyConfigurationObserver
name|d2
init|=
operator|new
name|DummyConfigurationObserver
argument_list|(
name|cm
argument_list|)
decl_stmt|;
name|cm
operator|.
name|notifyAllObservers
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|d1
operator|.
name|wasNotifiedOnChange
argument_list|()
argument_list|)
expr_stmt|;
name|d1
operator|.
name|resetNotifiedOnChange
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|d2
operator|.
name|wasNotifiedOnChange
argument_list|()
argument_list|)
expr_stmt|;
name|d2
operator|.
name|resetNotifiedOnChange
argument_list|()
expr_stmt|;
comment|// Now try deregistering an observer and verify that it was not notified
name|d2
operator|.
name|deregister
argument_list|()
expr_stmt|;
name|cm
operator|.
name|notifyAllObservers
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|d1
operator|.
name|wasNotifiedOnChange
argument_list|()
argument_list|)
expr_stmt|;
name|d1
operator|.
name|resetNotifiedOnChange
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|d2
operator|.
name|wasNotifiedOnChange
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// Register an observer that will go out of scope immediately, allowing
comment|// us to test that out of scope observers are deregistered.
specifier|private
name|void
name|registerLocalObserver
parameter_list|(
name|ConfigurationManager
name|cm
parameter_list|)
block|{
operator|new
name|DummyConfigurationObserver
argument_list|(
name|cm
argument_list|)
expr_stmt|;
block|}
comment|/**    * Test if out-of-scope observers are deregistered on GC.    */
annotation|@
name|Test
specifier|public
name|void
name|testDeregisterOnOutOfScope
parameter_list|()
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|ConfigurationManager
name|cm
init|=
operator|new
name|ConfigurationManager
argument_list|()
decl_stmt|;
name|boolean
name|outOfScopeObserversDeregistered
init|=
literal|false
decl_stmt|;
comment|// On my machine, I was able to cause a GC after around 5 iterations.
comment|// If we do not cause a GC in 100k iterations, which is very unlikely,
comment|// there might be something wrong with the GC.
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100000
condition|;
name|i
operator|++
control|)
block|{
name|registerLocalObserver
argument_list|(
name|cm
argument_list|)
expr_stmt|;
name|cm
operator|.
name|notifyAllObservers
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// 'Suggest' the system to do a GC. We should be able to cause GC
comment|// atleast once in the 2000 iterations.
name|System
operator|.
name|gc
argument_list|()
expr_stmt|;
comment|// If GC indeed happened, all the observers (which are all out of scope),
comment|// should have been deregistered.
if|if
condition|(
name|cm
operator|.
name|getNumObservers
argument_list|()
operator|<=
name|i
condition|)
block|{
name|outOfScopeObserversDeregistered
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|outOfScopeObserversDeregistered
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Observers were not GC-ed! Something seems to be wrong."
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|outOfScopeObserversDeregistered
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

