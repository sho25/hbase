begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
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
name|util
operator|.
name|Writables
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

begin_class
specifier|public
class|class
name|TestHServerInfo
block|{
annotation|@
name|Test
specifier|public
name|void
name|testHashCodeAndEquals
parameter_list|()
block|{
name|HServerAddress
name|hsa1
init|=
operator|new
name|HServerAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|)
decl_stmt|;
name|HServerInfo
name|hsi1
init|=
operator|new
name|HServerInfo
argument_list|(
name|hsa1
argument_list|,
literal|1L
argument_list|,
literal|5678
argument_list|)
decl_stmt|;
name|HServerInfo
name|hsi2
init|=
operator|new
name|HServerInfo
argument_list|(
name|hsa1
argument_list|,
literal|1L
argument_list|,
literal|5678
argument_list|)
decl_stmt|;
name|HServerInfo
name|hsi3
init|=
operator|new
name|HServerInfo
argument_list|(
name|hsa1
argument_list|,
literal|2L
argument_list|,
literal|5678
argument_list|)
decl_stmt|;
name|HServerInfo
name|hsi4
init|=
operator|new
name|HServerInfo
argument_list|(
name|hsa1
argument_list|,
literal|1L
argument_list|,
literal|5677
argument_list|)
decl_stmt|;
name|HServerAddress
name|hsa2
init|=
operator|new
name|HServerAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|1235
argument_list|)
decl_stmt|;
name|HServerInfo
name|hsi5
init|=
operator|new
name|HServerInfo
argument_list|(
name|hsa2
argument_list|,
literal|1L
argument_list|,
literal|5678
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|hsi1
operator|.
name|hashCode
argument_list|()
argument_list|,
name|hsi2
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|hsi1
operator|.
name|equals
argument_list|(
name|hsi2
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|hsi1
operator|.
name|hashCode
argument_list|()
argument_list|,
name|hsi3
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|hsi1
operator|.
name|equals
argument_list|(
name|hsi3
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|hsi1
operator|.
name|hashCode
argument_list|()
argument_list|,
name|hsi4
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|hsi1
operator|.
name|equals
argument_list|(
name|hsi4
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|hsi1
operator|.
name|hashCode
argument_list|()
argument_list|,
name|hsi5
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|hsi1
operator|.
name|equals
argument_list|(
name|hsi5
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHServerInfoHServerInfo
parameter_list|()
block|{
name|HServerAddress
name|hsa1
init|=
operator|new
name|HServerAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|)
decl_stmt|;
name|HServerInfo
name|hsi1
init|=
operator|new
name|HServerInfo
argument_list|(
name|hsa1
argument_list|,
literal|1L
argument_list|,
literal|5678
argument_list|)
decl_stmt|;
name|HServerInfo
name|hsi2
init|=
operator|new
name|HServerInfo
argument_list|(
name|hsi1
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|hsi1
argument_list|,
name|hsi2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testGetServerAddress
parameter_list|()
block|{
name|HServerAddress
name|hsa1
init|=
operator|new
name|HServerAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|)
decl_stmt|;
name|HServerInfo
name|hsi1
init|=
operator|new
name|HServerInfo
argument_list|(
name|hsa1
argument_list|,
literal|1L
argument_list|,
literal|5678
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|hsi1
operator|.
name|getServerAddress
argument_list|()
argument_list|,
name|hsa1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testToString
parameter_list|()
block|{
name|HServerAddress
name|hsa1
init|=
operator|new
name|HServerAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|)
decl_stmt|;
name|HServerInfo
name|hsi1
init|=
operator|new
name|HServerInfo
argument_list|(
name|hsa1
argument_list|,
literal|1L
argument_list|,
literal|5678
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|hsi1
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testReadFields
parameter_list|()
throws|throws
name|IOException
block|{
name|HServerAddress
name|hsa1
init|=
operator|new
name|HServerAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|)
decl_stmt|;
name|HServerInfo
name|hsi1
init|=
operator|new
name|HServerInfo
argument_list|(
name|hsa1
argument_list|,
literal|1L
argument_list|,
literal|5678
argument_list|)
decl_stmt|;
name|HServerAddress
name|hsa2
init|=
operator|new
name|HServerAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|1235
argument_list|)
decl_stmt|;
name|HServerInfo
name|hsi2
init|=
operator|new
name|HServerInfo
argument_list|(
name|hsa2
argument_list|,
literal|1L
argument_list|,
literal|5678
argument_list|)
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|Writables
operator|.
name|getBytes
argument_list|(
name|hsi1
argument_list|)
decl_stmt|;
name|HServerInfo
name|deserialized
init|=
operator|(
name|HServerInfo
operator|)
name|Writables
operator|.
name|getWritable
argument_list|(
name|bytes
argument_list|,
operator|new
name|HServerInfo
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|hsi1
argument_list|,
name|deserialized
argument_list|)
expr_stmt|;
name|bytes
operator|=
name|Writables
operator|.
name|getBytes
argument_list|(
name|hsi2
argument_list|)
expr_stmt|;
name|deserialized
operator|=
operator|(
name|HServerInfo
operator|)
name|Writables
operator|.
name|getWritable
argument_list|(
name|bytes
argument_list|,
operator|new
name|HServerInfo
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|hsa1
argument_list|,
name|deserialized
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompareTo
parameter_list|()
block|{
name|HServerAddress
name|hsa1
init|=
operator|new
name|HServerAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|1234
argument_list|)
decl_stmt|;
name|HServerInfo
name|hsi1
init|=
operator|new
name|HServerInfo
argument_list|(
name|hsa1
argument_list|,
literal|1L
argument_list|,
literal|5678
argument_list|)
decl_stmt|;
name|HServerAddress
name|hsa2
init|=
operator|new
name|HServerAddress
argument_list|(
literal|"localhost"
argument_list|,
literal|1235
argument_list|)
decl_stmt|;
name|HServerInfo
name|hsi2
init|=
operator|new
name|HServerInfo
argument_list|(
name|hsa2
argument_list|,
literal|1L
argument_list|,
literal|5678
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|hsi1
operator|.
name|compareTo
argument_list|(
name|hsi1
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|hsi2
operator|.
name|compareTo
argument_list|(
name|hsi2
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|int
name|compare1
init|=
name|hsi1
operator|.
name|compareTo
argument_list|(
name|hsi2
argument_list|)
decl_stmt|;
name|int
name|compare2
init|=
name|hsi2
operator|.
name|compareTo
argument_list|(
name|hsi1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
operator|(
name|compare1
operator|>
literal|0
operator|)
condition|?
name|compare2
operator|<
literal|0
else|:
name|compare2
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

