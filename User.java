public class User {
private long id ; 
private String full_name;
private String phone_number ;
private String email ;
private String pass_word;
private String role ;

public User(){

}
    public User(long id ,String full_name ,String phone_number, String email , String pass_word , String role){
     this.id = id;
     this.full_name =full_name ;
     this.phone_number =phone_number ;
     this.email = email ;
     this.pass_word =pass_word ;
     this.role = role ;  
    }
public long getId(){
    return id ;
}
public void setId(long id){
    this.id = id ;
}
public String getfull_name(){
    return full_name;
}
public void setfull_name(String full_name){
    this.full_name = full_name ;
}
public String getphone_number(){
    return phone_number ;
}
public void setphone_number(String phone_number){
    this.phone_number = phone_number ;
}
public String getemail(){
    return email ;
}
public void setemail(String email){
    this.email = email ;
}
public String getpass_word(){
    return pass_word ;
}
public void setpass_word(String pass_word){
    this.pass_word = pass_word ;
}
public String getrole(){
    return role ;
}
public void setrole(String role){
    this.role = role ;
}
}
